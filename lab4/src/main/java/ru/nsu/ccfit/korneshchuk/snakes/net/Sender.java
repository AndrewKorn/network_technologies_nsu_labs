package ru.nsu.ccfit.korneshchuk.snakes.net;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.Objects;

public class Sender implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Sender.class);
    @NotNull
    private final MessageStorage storage;

    @NotNull
    private final DatagramSocket socket;
    private final int nodeTimeoutMs;

    public Sender(@NotNull MessageStorage storage, @NotNull DatagramSocket socket, int nodeTimeoutMs) {
        this.storage = Objects.requireNonNull(storage, "Storage cant be null");
        this.socket = Objects.requireNonNull(socket, "Socket cant be null");
        if (nodeTimeoutMs < 0) {
            logger.error("Get negative node timeout = {}", nodeTimeoutMs);
            throw new IllegalArgumentException("Node timeout=" + nodeTimeoutMs + " cant be negative");
        }
        this.nodeTimeoutMs = nodeTimeoutMs;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            storage.resendUnconfirmedMessages();
            sendMessages();
            try {
                Thread.sleep(nodeTimeoutMs);
            } catch (InterruptedException e) {
                sendMessages();
                logger.error("Sender was interrupted while sleep", e);
                return;
            }
        }
    }

    public void addMessageToSend(@NotNull NetNode receiver, @NotNull Message message) {
        storage.addMessageToSend(receiver, message);
    }

    private void sendMessages() {
        Map<Message, NetNode> messagesToSend = storage.getMessagesToSend();
        messagesToSend.forEach((gameMessage, netNode) -> sendMessage(netNode, gameMessage));
    }

    private void sendMessage(@NotNull NetNode receiver, @NotNull Message message) {
        byte[] messageBytes = SerializationUtils.serialize(message);
        DatagramPacket packet = new DatagramPacket(
                messageBytes,
                messageBytes.length,
                receiver.getAddress(),
                receiver.getPort()
        );
        try {
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Cant send message={} to={}", message, receiver, e);
        }
    }
}
