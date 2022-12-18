package ru.nsu.ccfit.korneshchuk.snakes.presenter;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.gui.controllers.View;
import ru.nsu.ccfit.korneshchuk.snakes.gui.utils.GameInfoWithButton;
import ru.nsu.ccfit.korneshchuk.snakes.gui.utils.PlayerColorMapper;
import ru.nsu.ccfit.korneshchuk.snakes.net.MainNodeHandler;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.JoinToGameEvent;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.MoveEvent;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.UserEvent;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.UserEventType;
import ru.nsu.ccfit.korneshchuk.snakes.config.Config;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameObserver;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameState;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.Player;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.PlayerWithScore;
import ru.nsu.ccfit.korneshchuk.snakes.game.snake.SnakeInfo;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameInfo;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameListObserver;
import ru.nsu.ccfit.korneshchuk.snakes.net.node.Role;

import java.util.*;
import java.util.stream.Collectors;

public class MoveHandler implements GamePresenter, GameObserver, GameListObserver {
    @NotNull
    private final MainNodeHandler mainNodeHandler;

    private final Set<GameInfoWithButton> gameInfoWithButtons = new HashSet<>();

    private GameState prevGameState;
    private final View view;
    private String masterName;

    private final @NotNull Config playerConfig;

    @NotNull
    private final PlayerColorMapper colorMapper;

    public MoveHandler(@NotNull Config playerConfig, @NotNull MainNodeHandler mainNodeHandler, @NotNull View view) {
        this.playerConfig = Objects.requireNonNull(playerConfig, "Config cant be null");
        this.mainNodeHandler = Objects.requireNonNull(mainNodeHandler, "Node handler cant be null");
        this.mainNodeHandler.setConfig(playerConfig);
        this.mainNodeHandler.addObserver(this);
        this.mainNodeHandler.addGameListObserver(this);
        this.colorMapper = new PlayerColorMapper();
        this.view = view;
    }

    @Override
    public void fireEvent(@NotNull UserEvent userEvent) {
        Objects.requireNonNull(userEvent, "User event cant be null");
        if (userEvent.getType() == UserEventType.MOVE) {
            handleMoveEvent((MoveEvent) userEvent);
        } else if (userEvent.getType() == UserEventType.NEW_GAME) {
            handleNewGameEvent();
        } else if (userEvent.getType() == UserEventType.EXIT) {
            handleExitEvent();
        } else if (userEvent.getType() == UserEventType.JOIN_GAME) {
            handleJoinEvent((JoinToGameEvent) userEvent);
        }
    }

    private void handleJoinEvent(JoinToGameEvent userEvent) {
        view.setConfig(userEvent.getConfig());
        masterName = userEvent.getMasterName();
        mainNodeHandler.joinToGame(userEvent.getMasterNode(), playerConfig.getPlayerName());
    }

    private void handleExitEvent() {
        mainNodeHandler.exit();
    }

    private void handleNewGameEvent() {
        view.setConfig(playerConfig);
        mainNodeHandler.changeNodeRole(Role.MASTER);
        masterName = playerConfig.getPlayerName();
    }

    private void handleMoveEvent(MoveEvent event) {
        mainNodeHandler.handleMove(event.getDirection());
    }

    @Override
    public void update(@NotNull GameState gameState) {
        if (prevGameState != null) {
            clearPrevGameState(gameState);
        }
        updatePlayersColors(gameState.getActivePlayers());
        gameState.getFruits().forEach(view::drawFruit);
        updateSnakes(gameState);
        view.updateCurrentGameInfo(
                masterName == null ? "" : masterName,
                gameState.getGameConfig().getFieldHeight(),
                gameState.getGameConfig().getFieldWidth(),
                gameState.getFruits().size()
        );
        view.showUserListInfo(gameState.getActivePlayers());
        prevGameState = gameState;
    }

    private void updatePlayersColors(List<PlayerWithScore> activePlayers) {
        List<Player> players = activePlayers.stream()
                .map(PlayerWithScore::getPlayer)
                .collect(Collectors.toList());
        removeInactivePlayersFromColorMap(players);
        players.forEach(activePlayer -> {
            if (!colorMapper.isPlayerRegistered(activePlayer)) {
                colorMapper.addPlayer(activePlayer);
            }
        });
    }

    private void removeInactivePlayersFromColorMap(List<Player> players) {
        List<Player> inactiveRegisteredUsers = colorMapper.getRegisteredPlayers().stream()
                .filter(registeredPlayer -> !players.contains(registeredPlayer))
                .collect(Collectors.toList());
        inactiveRegisteredUsers.forEach(colorMapper::removePlayer);
    }

    private void updateSnakes(GameState gameState) {
        gameState.getSnakeInfos().forEach(this::drawSnakeBySnakeInfo);
    }

    private void drawSnakeBySnakeInfo(SnakeInfo snakeInfo) {
        if (snakeInfo.isZombieSnake()) {
            Color zombieSnakeColor = colorMapper.getZombieSnakeColor();
            snakeInfo.getSnakePoints().forEach(point -> view.drawSnakePoint(point, zombieSnakeColor));
            return;
        }
        Color playerColor = colorMapper
                .getColor(
                        snakeInfo.getPlayer().orElseThrow()
                )
                .orElseThrow(() -> new NoSuchElementException("Color map dont contain player"));
        view.drawSnakePoint(snakeInfo.getSnakeHead(), playerColor);
        view.drawSnakePoint(snakeInfo.getSnakeTail(), playerColor);
    }

    private void clearPrevGameState(GameState newGameState) {
        if (prevGameState.getSnakeInfos().size() != newGameState.getSnakeInfos().size()) {
            clearDeadSnakes(newGameState);
        }
        clearSnakesTails(newGameState);
    }

    private void clearSnakesTails(GameState newGameState) {
        List<Point> newSnakeTails = newGameState.getSnakeInfos().stream()
                .map(SnakeInfo::getSnakeTail).collect(Collectors.toList());
        prevGameState.getSnakeInfos().stream()
                .map(SnakeInfo::getSnakeTail)
                .filter(prevTail -> !newSnakeTails.contains(prevTail)
                )
                .forEach(view::drawEmptyCell);
    }

    private void clearDeadSnakes(GameState newGameState) {
        prevGameState.getSnakeInfos().stream()
                .filter(snakeInfo ->
                        isSnakeDead(snakeInfo, newGameState.getSnakeInfos()))
                .flatMap(snakeInfo ->
                        snakeInfo.getSnakePoints().stream())
                .forEach(view::drawEmptyCell);
    }

    private boolean isSnakeDead(SnakeInfo snake, List<SnakeInfo> snakeInfoList) {
        Point snakeHead = snake.getSnakeHead();
        return snakeInfoList.stream()
                .flatMap(snakeInfo ->
                        snakeInfo.getSnakePoints().stream()
                )
                .noneMatch(point ->
                        point.equals(snakeHead)
                );
    }

    @Override
    public void updateGameList(@NotNull Collection<GameInfo> gameInfos) {
        gameInfos.forEach(gameInfo -> gameInfoWithButtons.add(new GameInfoWithButton(gameInfo)));
        view.showGameList(Set.copyOf(gameInfoWithButtons));
    }
}
