package ru.nsu.ccfit.korneshchuk.attachment;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public class AcceptHandler extends Attachment {
    private static final Logger logger = LoggerFactory.getLogger(AcceptHandler.class);

    @NotNull
    private final ServerSocketChannel serverSocketChannel;

    public AcceptHandler(@NotNull ServerSocketChannel serverSocketChannel) {
        super(AttachmentType.ACCEPT_HANDLER);
        this.serverSocketChannel = Objects.requireNonNull(serverSocketChannel, "Server socket channel cant be null");
    }

    @NotNull
    public SocketChannel accept() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            return socketChannel;
        } catch (IOException e) {
            logger.error("Error while accept", e);
            throw new IllegalStateException("Error while accept", e);
        }
    }
}
