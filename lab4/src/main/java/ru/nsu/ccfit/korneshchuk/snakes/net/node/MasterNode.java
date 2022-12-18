package ru.nsu.ccfit.korneshchuk.snakes.net.node;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.snakes.config.Config;
import ru.nsu.ccfit.korneshchuk.snakes.game.Direction;
import ru.nsu.ccfit.korneshchuk.snakes.game.Game;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameObserver;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameState;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.Player;
import ru.nsu.ccfit.korneshchuk.snakes.net.GameRecoveryInformation;
import ru.nsu.ccfit.korneshchuk.snakes.net.Neighbor;
import ru.nsu.ccfit.korneshchuk.snakes.net.NetNode;
import ru.nsu.ccfit.korneshchuk.snakes.net.NodeHandler;
import ru.nsu.ccfit.korneshchuk.snakes.net.messagehandler.JoinMessageHandler;
import ru.nsu.ccfit.korneshchuk.snakes.net.messagehandler.PingMessageHandler;
import ru.nsu.ccfit.korneshchuk.snakes.net.messagehandler.RoleChangeMessageHandler;
import ru.nsu.ccfit.korneshchuk.snakes.net.messagehandler.SteerMessageHandler;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.*;
import ru.nsu.ccfit.korneshchuk.snakes.utils.DurationUtils;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MasterNode implements
        GameNode,
        RoleChangeMessageHandler,
        JoinMessageHandler,
        SteerMessageHandler,
        PingMessageHandler,
        GameObserver {
    private static final Logger logger = LoggerFactory.getLogger(MasterNode.class);
    public static final int ANNOUNCEMENT_SEND_PERIOD = 1000;

    private final Game game;
    private final @NotNull Config gameConfig;
    private final Map<Player, Direction> playersMoves = new ConcurrentHashMap<>();
    private final Map<Neighbor, Player> registeredNodesAsPlayers = new ConcurrentHashMap<>();
    private final Player currentPlayer;
    private NodeHandler nodeHandler;
    private final Timer timer = new Timer();

    private NetNode deputy;

    public MasterNode(@NotNull Config gameConfig) {
        this.gameConfig = Objects.requireNonNull(gameConfig);
        this.game = new Game(gameConfig);
        this.game.addObserver(this);
        this.currentPlayer = game.registrationNewPlayer(gameConfig.getPlayerName());
        startGameUpdateTimer();
        startSendAnnouncementMessages();
        startRemovingDisconnectedPlayers();
    }

    public MasterNode(@NotNull Config config, @NotNull GameRecoveryInformation recoveryInformation) {
        this.gameConfig = Objects.requireNonNull(config);
        this.game = new Game(recoveryInformation.getGameState());
        this.game.addObserver(this);
        this.currentPlayer = Player.create(gameConfig.getPlayerName());
        recoveryInformation.getRegisteredPlayers().forEach((neighbor, player) -> {
            if (!player.equals(currentPlayer)) {
                registerNewPlayer(neighbor, player.getName());
            }
        });
        startGameUpdateTimer();
        startSendAnnouncementMessages();
        startRemovingDisconnectedPlayers();
    }

    private void startSendAnnouncementMessages() {
        TimerTask announcementSendTask = new TimerTask() {
            @Override
            public void run() {
                nodeHandler.sendMessage(
                        new NetNode(nodeHandler.getMulticastInfo()),
                        new AnnouncementMessage(gameConfig, registeredNodesAsPlayers.size() + 1, true)
                );
            }
        };
        timer.schedule(announcementSendTask, 0, ANNOUNCEMENT_SEND_PERIOD);
    }

    private void registerNewMove(@NotNull Player player, @NotNull Direction direction) {
        playersMoves.put(
                Objects.requireNonNull(player),
                Objects.requireNonNull(direction)
        );
    }

    private void startGameUpdateTimer() {
        TimerTask gameUpdateTask = new TimerTask() {
            @Override
            public void run() {
                game.makeAllPlayersMove(Map.copyOf(playersMoves));
                playersMoves.clear();
            }
        };
        timer.schedule(gameUpdateTask, 0, gameConfig.getStateDelayMs());
    }

    @Override
    public void update(@NotNull GameState gameState) {
        registeredNodesAsPlayers.keySet()
                .forEach(netNode ->
                        nodeHandler.sendMessage(
                                netNode,
                                new StateMessage(gameState, Map.copyOf(registeredNodesAsPlayers))
                        )
                );
        nodeHandler.updateState(gameState);
    }


    @Override
    public void handleMessage(@NotNull NetNode sender, @NotNull Message message) {
        Objects.requireNonNull(message, "Message cant be null");
        Objects.requireNonNull(sender, "Sender cant be null");
        switch (message.getType()) {
            case ROLE_CHANGE -> handle(sender, (RoleChangeMessage) message);
            case STEER -> handle(sender, (SteerMessage) message);
            case JOIN -> handle(sender, (JoinMessage) message);
            case PING -> handle(sender, (PingMessage) message);
            default -> throw new IllegalStateException("Cant handle this message type = " + message.getType());
        }
    }

    public void startRemovingDisconnectedPlayers() {
        TimerTask removeTask = getRemoveDisconnectedUsersTask();
        timer.schedule(removeTask, 0, (long) gameConfig.getPingDelayMs() * 10);
    }

    @NotNull
    private TimerTask getRemoveDisconnectedUsersTask() {
        return new TimerTask() {
            @Override
            public void run() {
                registeredNodesAsPlayers.forEach((neighbor, player) -> {
                    if (isDisconnected(neighbor)) {
                        game.removePlayer(player);
                        playersMoves.remove(player);
                    }
                });
                registeredNodesAsPlayers.keySet().removeIf(MasterNode.this::isDisconnected);
                if (deputy != null && !registeredNodesAsPlayers.containsKey(deputy)) {
                    deputy = null;
                    chooseNewDeputy();
                }
            }
        };
    }

    private void chooseNewDeputy() {
        Optional<Neighbor> neighborOpt = registeredNodesAsPlayers.keySet().stream().findAny();
        neighborOpt.ifPresentOrElse(
                this::setDeputy,
                () -> logger.warn("Cant chose deputy")
        );
    }

    private void setDeputy(@NotNull NetNode deputy) {
        this.deputy = deputy;
        nodeHandler.sendMessage(
                deputy,
                new RoleChangeMessage(Role.MASTER, Role.DEPUTY)
        );
    }

    private boolean isDisconnected(@NotNull Neighbor node) {
        return DurationUtils.betweenInMs(node.getLastSeenTime(), Instant.now()) >= gameConfig.getNodeTimeoutMs();
    }

    @Override
    public void setNodeHandler(@NotNull NodeHandler nodeHandler) {
        this.nodeHandler = nodeHandler;
    }


    @Override
    public void handle(@NotNull NetNode sender, @NotNull JoinMessage joinMsg) {
        if (registeredNodesAsPlayers.isEmpty()) {
            setDeputy(sender);
        }
        Neighbor neighbor = new Neighbor(sender);
        validateNewPlayer(sender, joinMsg.getPlayerName(), neighbor);
        registerNewPlayer(neighbor, joinMsg.getPlayerName())
                .ifPresent(player ->
                        logger.debug("NetNode={} was successfully registered as player={}", sender, player)
                );
    }

    private void validateNewPlayer(@NotNull NetNode sender, @NotNull String playerName, Neighbor neighbor) {
        if (registeredNodesAsPlayers.containsKey(neighbor)) {
            logger.error("Node={} already registered as player={}", sender, registeredNodesAsPlayers.get(neighbor));
            nodeHandler.sendMessage(sender, new ErrorMessage("Player already exist"));
            throw new IllegalArgumentException("Node={" + sender + "} already registered");
        } else if (currentPlayer.equals(Player.create(playerName))) {
            logger.error("Node={} trying register with master name={}", sender, playerName);
            nodeHandler.sendMessage(sender, new ErrorMessage("Player name is taken by master"));
            throw new IllegalArgumentException("Try register with master name=" + playerName);
        }
    }

    @NotNull
    private Optional<Player> registerNewPlayer(@NotNull Neighbor sender, @NotNull String playerName) {
        try {
            Player player = game.registrationNewPlayer(playerName);
            registeredNodesAsPlayers.put(sender, player);
            return Optional.of(player);
        } catch (IllegalStateException e) {
            logger.debug("Cant place player on field because no space");
            nodeHandler.sendMessage(sender, new ErrorMessage("Cant place player on field because no space"));
            return Optional.empty();
        }

    }

    @Override
    public void handle(@NotNull NetNode sender, @NotNull RoleChangeMessage roleChangeMsg) {
        if (roleChangeMsg.getFromRole() == Role.VIEWER && roleChangeMsg.getToRole() == Role.MASTER) {
            removePlayer(sender);
        } else {
            logger.warn("Unsupported roles at role change message={} from={}", roleChangeMsg, sender);
            throw new IllegalArgumentException("Unsupported roles at role change message=" + roleChangeMsg + " from=" + sender);
        }
    }

    private void removePlayer(NetNode sender) {
        checkRegistration(sender);
        Player player = registeredNodesAsPlayers.get(sender);
        registeredNodesAsPlayers.remove(sender);
        playersMoves.remove(player);
        game.removePlayer(player);
    }

    private void checkRegistration(@NotNull NetNode sender) {
        if (!registeredNodesAsPlayers.containsKey(sender)) {
            logger.error("Node={} is not registered", sender);
            throw new IllegalArgumentException("Node={" + sender + "} is not registered");
        }
    }

    @Override
    public void handle(@NotNull NetNode sender, @NotNull SteerMessage steerMsg) {
        checkRegistration(sender);
        updateLastSeen(sender);
        Player senderAsPlayer = registeredNodesAsPlayers.get(sender);
        registerNewMove(senderAsPlayer, steerMsg.getDirection());
        logger.debug("NetNode={} as player={} make move with direction={}", sender, senderAsPlayer, steerMsg.getDirection());
    }

    @Override
    public void handle(@NotNull NetNode sender, @NotNull PingMessage pingMessageHandler) {
        updateLastSeen(sender);
    }

    private void updateLastSeen(@NotNull NetNode sender) {
        registeredNodesAsPlayers.keySet().forEach(neighbor -> {
            if (neighbor.equals(sender)) {
                neighbor.updateLastSeenTime();
            }
        });
    }

    @Override
    public void makeMove(@NotNull Direction direction) {
        registerNewMove(currentPlayer, direction);
    }

    @Override
    public void stop() {
        if (deputy != null) {
            nodeHandler.sendMessage(deputy, new RoleChangeMessage(Role.MASTER, Role.MASTER));
        }
        timer.cancel();
    }
}
