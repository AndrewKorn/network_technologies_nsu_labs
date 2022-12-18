package ru.nsu.ccfit.korneshchuk.attachment;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.socks.SOCKSErrorCode;
import ru.nsu.ccfit.korneshchuk.utils.SelectionKeyUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public class ServerHandler extends Attachment implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private static final int BUFFER_SIZE = 1024;


    @NotNull
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    @NotNull
    private final SelectionKey serverKey;
    @NotNull
    private final SelectionKey clientKey;
    @NotNull
    private final SocketChannel socketChannel;

    private boolean isConnected = false;
    private boolean isEndOfStream = false;

    public ServerHandler(@NotNull SelectionKey serverKey, @NotNull SelectionKey clientKey) {
        super(AttachmentType.SERVER_HANDLER);
        this.clientKey = Objects.requireNonNull(clientKey, "Client key cant be null");
        this.serverKey = Objects.requireNonNull(serverKey, "Server key cant be null");
        this.socketChannel = (SocketChannel) serverKey.channel();
    }

    public void sendDataToServer() {
        ClientHandler clientHandler = getClientHandler();
        try {
            socketChannel.write(clientHandler.getBuffer());
        } catch (IOException e) {
            logger.error("Error while write", e);
            closeWithoutException();
            throw new IllegalStateException("Error while write", e);
        }
        if (!clientHandler.getBuffer().hasRemaining()) {
            SelectionKeyUtils.turnOffWriteOption(serverKey);
            SelectionKeyUtils.turnOnReadOption(clientKey);
        }
    }

    @NotNull
    private ClientHandler getClientHandler() {
        Attachment attachment = (Attachment) clientKey.attachment();
        if (attachment.getType() != AttachmentType.CLIENT_HANDLER) {
            logger.error("Client key attachment is not client handler and has type={}", attachment.getType());
            throw new IllegalStateException("Client key attachment is not client handler and has type=" + attachment.getType());
        }
        return (ClientHandler) attachment;
    }

    public void getDataFromServer() {
        buffer.clear();
        int readBytes = -1;
        try {
            readBytes = socketChannel.read(buffer);
        } catch (IOException e) {
            logger.error("Error while reading");
            throw new IllegalStateException("Error while reading");
        }
        if (readBytes == -1) {
            handleEndOfStream();
            return;
        }
        buffer.flip();
        SelectionKeyUtils.turnOffReadOption(serverKey);
        SelectionKeyUtils.turnOnWriteOption(clientKey);
    }

    private void handleEndOfStream() {
        isEndOfStream = true;
        SelectionKeyUtils.turnOffReadOption(serverKey);
        ClientHandler clientHandler = getClientHandler();
        clientHandler.shutdownOutput();
        if (clientHandler.isEndOfStream()) {
            closeWithoutException();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect() {
        if (isConnected) {
            throw new IllegalStateException("Already connected");
        }
        if (serverKey.isConnectable()) {
            checkFinishConnect();
            ClientHandler clientHandler = getClientHandler();
            clientHandler.createResponse(SOCKSErrorCode.SUCCESS);
            serverKey.interestOps(SelectionKey.OP_READ);
            buffer.clear();
            buffer.flip();
            isConnected = true;
        }
    }

    private void checkFinishConnect() {
        try {
            if (!socketChannel.finishConnect()) {
                throw new IllegalStateException("Cant connect");
            }
        } catch (IOException e) {
            logger.error("Error while finish connect", e);
            throw new IllegalStateException("Error while finish connect", e);
        }
    }

    @NotNull
    public ByteBuffer getBuffer() {
        return buffer;
    }


    public boolean isEndOfStream() {
        return isEndOfStream;
    }

    public void shutdownOutput() {
        try {
            socketChannel.shutdownOutput();
        } catch (IOException e) {
            logger.error("Error while shutdown output", e);
            throw new UncheckedIOException("Error while shutdown output", e);
        }
    }

    public void closeWithoutException() {
        try {
            socketChannel.close();
            clientKey.channel().close();
        } catch (IOException e) {
            logger.error("Error while closing", e);
        }
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
        clientKey.channel().close();
    }

    public boolean isReadable() {
        return serverKey.isReadable();
    }

    public boolean isWritable() {
        return serverKey.isWritable();
    }
}