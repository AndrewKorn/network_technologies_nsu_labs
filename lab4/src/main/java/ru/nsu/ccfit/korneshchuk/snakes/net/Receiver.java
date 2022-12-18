package ru.nsu.ccfit.korneshchuk.snakes.net;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.AckMessage;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.Optional;

public class Receiver implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private static final int INITIAL_PACKET_SIZE = 8096;
    private static final int MAX_PACKET_SIZE = 65535;
    private static final int SO_TIMEOUT_MS = 3000;

    @NotNull
    private final MessageStorage storage;

    @NotNull
    private final DatagramSocket socket;
    private int packetSize;

    public Receiver(@NotNull MessageStorage storage, @NotNull DatagramSocket socket) {
        this.storage = Objects.requireNonNull(storage, "Storage cant be null");
        this.socket = Objects.requireNonNull(socket, "Socket cant be null");
        this.packetSize = INITIAL_PACKET_SIZE;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(SO_TIMEOUT_MS);
        } catch (SocketException e) {
            logger.error("Socket exception while set soTimeout={}", SO_TIMEOUT_MS, e);
        }
        while (!Thread.currentThread().isInterrupted()) {
            DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
            try {
                socket.receive(packet);
                NetNode sender = parseSender(packet);
                parseMessage(packet)
                        .ifPresent(message -> {
                                    storage.addReceivedMessage(sender, message);
                                    if (message.getType().isNeedConfirmation()) {
                                        storage.addMessageToSend(sender, new AckMessage(message.getUuid()));
                                    }
                                }
                        );
            } catch (SocketTimeoutException e) {
                logger.warn("Exceed socket timeout");
            } catch (IOException e) {
                logger.error("Cant receive packet", e);
            }
        }
    }

    @NotNull
    private Optional<Message> parseMessage(@NotNull DatagramPacket packet) {
        try {
            return Optional.of(SerializationUtils.deserialize(packet.getData()));
        } catch (ClassCastException e) {
            logger.error("Cant deserialize this class, is not message instance");
            return Optional.empty();
        } catch (SerializationException e) {
            logger.error("Cant deserialize message because not enough current packet size={}", packetSize, e);
            increasePackageSize();
            return Optional.empty();
        }
    }

    private void increasePackageSize() {
        if (packetSize * 2 > MAX_PACKET_SIZE) {
            logger.warn("Cant resize packet because maximum packet size reached, max={}", MAX_PACKET_SIZE);
            packetSize = MAX_PACKET_SIZE;
        } else {
            packetSize *= 2;
        }
    }

    @NotNull
    private NetNode parseSender(@NotNull DatagramPacket packet) {
        return new NetNode(packet.getAddress(), packet.getPort());
    }
}
