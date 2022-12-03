package ru.nsu.ccfit.korneshchuk.snakes.net;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.snakes.config.Config;
import ru.nsu.ccfit.korneshchuk.snakes.game.Direction;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameObservable;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameObserver;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameState;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameInfo;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameListChecker;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameListObservable;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameListObserver;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.JoinMessage;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.Message;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.PingMessage;
import ru.nsu.ccfit.korneshchuk.snakes.net.node.GameNode;
import ru.nsu.ccfit.korneshchuk.snakes.net.node.MasterNode;
import ru.nsu.ccfit.korneshchuk.snakes.net.node.NodeFactory;
import ru.nsu.ccfit.korneshchuk.snakes.net.node.Role;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;

public class MainNodeHandler implements NodeHandler, GameObservable, GameListObserver, GameListObservable {
    private static final Logger logger = LoggerFactory.getLogger(MainNodeHandler.class);

    @NotNull
    private final List<GameObserver> gameStateObservers = new ArrayList<>();
    @NotNull
    private final List<GameListObserver> gameListObservers = new ArrayList<>();
    private final Sender sender;
    private final Thread senderThread;
    private final Thread receiverThread;
    @NotNull
    private final InetSocketAddress multicastInfo;

    private NetNode master;

    private Role nodeRole;
    private GameNode gameNode;
    private final MessageStorage messageStorage;
    private Config config;
    private GameState gameState;

    private final Timer timer = new Timer();
    private final GameListChecker gameListChecker;

    public MainNodeHandler(@NotNull Role nodeRole,
                           @NotNull Config config,
                           int port,
                           @NotNull InetAddress multicastAddress,
                           int multicastPort) {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            logger.error("Cant create socket", e);
            throw new IllegalStateException("Cant create socket", e);
        }
        messageStorage = new MessageStorage();
        try {
            sender = new Sender(messageStorage, socket, config.getPingDelayMs());
            gameListChecker = new GameListChecker(multicastAddress, multicastPort);
        } catch (RuntimeException e) {
            exit();
            logger.error("Catch exception while creating sender or game list checker", e);
            throw new IllegalStateException("Catch exception while creating sender or game list checker", e);
        }
        setConfig(config);
        Receiver receiver = new Receiver(messageStorage, socket);
        senderThread = new Thread(sender);
        receiverThread = new Thread(receiver);
        senderThread.start();
        receiverThread.start();
        multicastInfo = new InetSocketAddress(multicastAddress, multicastPort);
        gameListChecker.addGameListObserver(this);
        gameListChecker.start();
        startSendPingMessages();
        startHandleReceivedMessages();
        changeNodeRole(nodeRole);
        startSendPingMessages();
    }

    private void startHandleReceivedMessages() {
        timer.schedule(getReceivedMessageHandler(), 0, 100);
    }

    public void startSendPingMessages() {
        timer.schedule(getPingTimerTask(), 0, config.getPingDelayMs());
    }


    @NotNull
    private TimerTask getReceivedMessageHandler() {
        return new TimerTask() {
            @Override
            public void run() {
                messageStorage.getReceivedMessages().forEach((message, netNode) -> gameNode.handleMessage(netNode, message));
            }
        };
    }

    public @NotNull InetSocketAddress getMulticastInfo() {
        return multicastInfo;
    }

    @NotNull
    private TimerTask getPingTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (master != null) {
                    messageStorage.addMessageToSend(master, new PingMessage());
                }
            }
        };
    }

    public void setConfig(@NotNull Config config) {
        this.config = Objects.requireNonNull(config, "Config cant be null");
    }

    public void joinToGame(@NotNull NetNode gameOwner,
                           @NotNull String playerName) {
        sender.addMessageToSend(
                gameOwner,
                new JoinMessage(playerName)
        );
        master = gameOwner;
        gameNode = NodeFactory.createNode(Role.NORMAL, config);
        gameNode.setNodeHandler(this);
    }

    @Override
    public void changeNodeRole(@NotNull Role nodeRole) {
        if (config == null) {
            logger.error("Cant change role={} to {} without config", this.nodeRole, nodeRole);
            exit();
            throw new IllegalStateException("Cant change role without config");
        }
        if (gameNode != null) {
            gameNode.stop();
        }
        this.nodeRole = nodeRole;
        gameNode = NodeFactory.createNode(
                Objects.requireNonNull(nodeRole),
                config
        );
        gameNode.setNodeHandler(this);
    }

    @Override
    public void changeNodeRole(@NotNull Role nodeRole, @NotNull GameRecoveryInformation gameRecoveryInformation) {
        if (nodeRole != Role.MASTER) {
            exit();
            logger.error("Cant change role with game state if is not master, actual={}", nodeRole);
            throw new IllegalStateException("Cant change role with game state if is not master, actual" + nodeRole);
        }
        gameNode = new MasterNode(config, gameRecoveryInformation);
        gameNode.setNodeHandler(this);
        master = null;
    }


    @Override
    public void sendMessage(@NotNull NetNode receiver, @NotNull Message message) {
        messageStorage.addMessageToSend(receiver, message);
    }

    @Override
    public void updateState(@NotNull GameState gameState) {
        this.gameState = gameState;
        notifyObservers();
    }

    @Override
    public void showError(@NotNull String errorMessage) {
        System.err.println("ERROR=" + errorMessage);
    }

    @Override
    public void setMaster(@NotNull NetNode newMasterNode) {
        this.master = Objects.requireNonNull(newMasterNode, "Master node cant be null");
    }

    @Override
    public void lose() {
        System.out.println("Lose");
    }

    @Override
    public void addObserver(@NotNull GameObserver gameObserver) {
        gameStateObservers.add(Objects.requireNonNull(gameObserver));
    }

    @Override
    public void removeObserver(@NotNull GameObserver gameObserver) {
        gameStateObservers.remove(gameObserver);
    }

    @Override
    public void addGameListObserver(@NotNull GameListObserver observer) {
        gameListObservers.add(Objects.requireNonNull(observer));
    }

    @Override
    public void removeGameListObserver(@NotNull GameListObserver observer) {
        gameListObservers.remove(Objects.requireNonNull(observer));
    }

    @Override
    public void notifyObservers() {
        GameState state = this.gameState;
        gameStateObservers.forEach(gameObserver -> gameObserver.update(state));
    }

    @Override
    @NotNull
    public NetNode getMaster() {
        return master;
    }

    public void handleMove(@NotNull Direction direction) {
        Objects.requireNonNull(direction, "Direction cant be null");
        gameNode.makeMove(direction);
    }

    public void exit() {
        if (gameListChecker != null) {
            gameListChecker.stop();
        }
        if (gameNode != null) {
            gameNode.stop();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        if (receiverThread != null) {
            senderThread.interrupt();
        }
        timer.cancel();
    }

    @Override
    public void updateGameList(@NotNull Collection<GameInfo> gameInfos) {
        gameListObservers.forEach(gameListObserver -> gameListObserver.updateGameList(gameInfos));
    }
}
