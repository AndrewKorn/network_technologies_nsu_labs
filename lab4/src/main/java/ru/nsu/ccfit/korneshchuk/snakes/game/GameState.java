package ru.nsu.ccfit.korneshchuk.snakes.game;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.config.GameConfig;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.PlayerWithScore;
import ru.nsu.ccfit.korneshchuk.snakes.game.snake.SnakeInfo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameState implements Serializable {

    @NotNull
    private final List<Point> fruits;


    @NotNull
    private final List<PlayerWithScore> activePlayers;


    @NotNull
    private final List<SnakeInfo> snakeInfos;

    @NotNull
    private final GameConfig gameConfig;

    private final int stateID;


    public GameState(@NotNull List<Point> fruits,
                     @NotNull List<PlayerWithScore> activePlayers,
                     @NotNull List<SnakeInfo> snakeInfos,
                     @NotNull GameConfig gameConfig,
                     int stateID) {
        this.fruits = Collections.unmodifiableList(
                Objects.requireNonNull(fruits, "Fruits list cant be null")
        );
        this.activePlayers = Collections.unmodifiableList(
                Objects.requireNonNull(activePlayers, "Active players list cant be null")
        );
        this.snakeInfos = Collections.unmodifiableList(
                Objects.requireNonNull(snakeInfos, "Snake infos list cant be null")
        );
        this.gameConfig = Objects.requireNonNull(gameConfig, "Game config cant be null");
        this.stateID = stateID;
    }

    public int getStateID() {
        return stateID;
    }


    @NotNull
    public List<Point> getFruits() {
        return fruits;
    }


    @NotNull
    public List<PlayerWithScore> getActivePlayers() {
        return activePlayers;
    }


    @NotNull
    public List<SnakeInfo> getSnakeInfos() {
        return snakeInfos;
    }

    @NotNull
    public GameConfig getGameConfig() {
        return gameConfig;
    }
}
