package ru.nsu.ccfit.korneshchuk.attachment;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.dns.DNSResolver;
import ru.nsu.ccfit.korneshchuk.socks.SOCKSErrorCode;
import ru.nsu.ccfit.korneshchuk.utils.PortValidator;
import ru.nsu.ccfit.korneshchuk.utils.SelectionKeyUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ClientHandler extends Attachment implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final int BUFFER_SIZE = 1024;

    private static final byte VERSION = 0x05;
    private static final byte RESERVED_BYTE = 0x00;
    private static final byte IPV4 = 0x01;
    private static final byte DOMAIN_NAME = 0x03;
    private static final byte TCP_CONNECT_COMMAND = 0x01;
    private static final byte NO_AUTHENTICATION_REQUIRED = 0x00;
    private static final byte NO_ACCEPTABLE_METHODS = (byte) 0xFF;

    private static final int COMMAND_BYTE_INDEX = 1;
    private static final int ADDRESS_TYPE_BYTE_INDEX = 3;

    @NotNull
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private SelectionKey serverKey;
    private int portToConnect;
    private boolean isEndOfStream = false;

    private ClientHandlerState currentState;

    @NotNull
    private final SocketChannel socketChannel;
    @NotNull
    private final DNSResolver dnsResolver;
    @NotNull
    private final SelectionKey clientKey;


    public ClientHandler(@NotNull DNSResolver dnsResolver, @NotNull SelectionKey clientKey) {
        super(AttachmentType.CLIENT_HANDLER);
        this.dnsResolver = Objects.requireNonNull(dnsResolver, "DNS resolver cant be null");
        this.clientKey = Objects.requireNonNull(clientKey, "Client key cant be null");
        this.socketChannel = (SocketChannel) clientKey.channel();
        this.currentState = ClientHandlerState.CLIENT_AUTHENTICATION_WAITING;
    }

    public void changeState() {
        ClientHandlerState prevState = currentState;
        if (currentState == ClientHandlerState.CLIENT_AUTHENTICATION_WAITING) {
            doAuthentication();
        } else if (currentState == ClientHandlerState.CLIENT_REQUEST_WAITING) {
            handleRequest();
        } else if (currentState == ClientHandlerState.SEND_CLIENT_AUTHENTICATION && !buffer.hasRemaining()) {
            toRequestWaiting();
        } else if (currentState == ClientHandlerState.SEND_CLIENT_RESPONSE && !buffer.hasRemaining()) {
            toForwarding();
        } else if (currentState == ClientHandlerState.SEND_CLIENT_ERROR && !buffer.hasRemaining()) {
            handleSendingError();
        }
        logger.info("State was change from={} to {}", prevState, currentState);
    }

    private void toForwarding() {
        currentState = ClientHandlerState.FORWARDING;
        clientKey.interestOps(SelectionKey.OP_READ);
    }

    private void toRequestWaiting() {
        currentState = ClientHandlerState.CLIENT_REQUEST_WAITING;
        buffer.clear();
    }

    private void handleSendingError() {
        closeWithoutException();
        buffer.clear();
    }

    public void sendDataToClient() {
        ServerHandler serverHandler = getServerHandler();
        write(serverHandler.getBuffer());
        if (!serverHandler.getBuffer().hasRemaining()) {
            SelectionKeyUtils.turnOffWriteOption(clientKey);
            SelectionKeyUtils.turnOnReadOption(serverKey);
        }
    }

    @NotNull
    private ServerHandler getServerHandler() {
        Attachment attachment = (Attachment) serverKey.attachment();
        if (attachment.getType() != AttachmentType.SERVER_HANDLER) {
            logger.error("Server key attachment is not server handler and has type={}", attachment.getType());
            throw new IllegalStateException("Server key attachment is not server handler and has type=" + attachment.getType());
        }
        return (ServerHandler) attachment;
    }

    public void getDataFromClient() {
        buffer.clear();
        int readBytes = read();
        if (readBytes == -1) {
            handleEndOfStream();
            return;
        }
        buffer.flip();
        SelectionKeyUtils.turnOffReadOption(clientKey);
        SelectionKeyUtils.turnOnWriteOption(serverKey);
    }

    private void handleEndOfStream() {
        isEndOfStream = true;
        SelectionKeyUtils.turnOffReadOption(clientKey);
        ServerHandler serverHandler = getServerHandler();
        serverHandler.shutdownOutput();
        if (serverHandler.isEndOfStream()) {
            closeWithoutException();
        }
    }

    public void closeWithoutException() {
        try {
            socketChannel.close();
            if (serverKey != null) {
                serverKey.channel().close();
            }
        } catch (IOException e) {
            logger.error("Error while closing", e);
        }
    }

    public void handleRequest() {
        int bufferSize = buffer.position();
        if (bufferSize < 4) {
            return;
        }
        byte command = buffer.get(COMMAND_BYTE_INDEX);
        if (command != TCP_CONNECT_COMMAND) {
            createResponse(SOCKSErrorCode.COMMAND_NOT_SUPPORTED);
            return;
        }
        byte addressType = buffer.get(ADDRESS_TYPE_BYTE_INDEX);
        if (addressType == IPV4) {
            handleIPV4Address(bufferSize);
        } else if (addressType == DOMAIN_NAME) {
            handleDomainAddress(bufferSize);
        } else {
            createResponse(SOCKSErrorCode.ADDRESS_TYPE_NOT_SUPPORTED);
        }
    }

    private void handleDomainAddress(int bufferSize) {
        int addressLength = buffer.get(4);
        if (addressLength + 6 > bufferSize) {
            return;
        }
        byte[] address = new byte[addressLength];
        buffer.position(5);
        buffer.get(address, 0, addressLength);
        String addressStr = new String(address, StandardCharsets.UTF_8);
        clientKey.interestOps(0);
        currentState = ClientHandlerState.FORWARDING;
        dnsResolver.appendResolveRequest(addressStr, this);
        portToConnect = buffer.getShort(5 + addressLength);
    }

    private void handleIPV4Address(int bufferSize) {
        if (bufferSize < 10) {
            return;
        }
        byte[] address = new byte[4];
        buffer.position(4);
        buffer.get(address);
        int port = buffer.getShort(8);
        try {
            connect(InetAddress.getByAddress(address), port);
            clientKey.interestOps(0);
            currentState = ClientHandlerState.FORWARDING;
        } catch (UnknownHostException e) {
            logger.error("Cant get inet address by {}", address, e);
            throw new IllegalStateException("Cant get inet address", e);
        }
    }

    private void doAuthentication() {
        byte method = getMethod();
        buffer.clear();
        buffer.put(VERSION);
        buffer.put(method);
        buffer.flip();
        currentState = ClientHandlerState.SEND_CLIENT_AUTHENTICATION;
        if (method == NO_ACCEPTABLE_METHODS) {
            currentState = ClientHandlerState.SEND_CLIENT_ERROR;
        }
        clientKey.interestOps(SelectionKey.OP_WRITE);
    }

    private byte getMethod() {
        int methodsNumber = buffer.get(1);
        byte method = NO_ACCEPTABLE_METHODS;
        for (int i = 0; i < methodsNumber; i++) {
            byte currentMethod = buffer.get(i + 2);
            if (currentMethod == NO_AUTHENTICATION_REQUIRED) {
                method = currentMethod;
                break;
            }
        }
        return method;
    }

    public int read() {
        try {
            return socketChannel.read(buffer);
        } catch (IOException e) {
            logger.error("Error while reading", e);
            throw new UncheckedIOException("Error while reading", e);
        }
    }

    public void write() {
        write(buffer);
    }

    public void write(@NotNull ByteBuffer byteBuffer) {
        Objects.requireNonNull(byteBuffer, "Byte buffer cant be null");
        try {
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            logger.error("Error while writing", e);
            throw new UncheckedIOException("Error while writing", e);
        }
    }

    public boolean isWaiting() {
        return currentState == ClientHandlerState.CLIENT_AUTHENTICATION_WAITING || currentState == ClientHandlerState.CLIENT_REQUEST_WAITING;
    }

    public boolean isSending() {
        return currentState == ClientHandlerState.SEND_CLIENT_AUTHENTICATION ||
                currentState == ClientHandlerState.SEND_CLIENT_RESPONSE ||
                currentState == ClientHandlerState.SEND_CLIENT_ERROR;
    }

    public boolean isReadyToGetData() {
        return currentState == ClientHandlerState.FORWARDING && clientKey.isReadable();
    }

    public boolean isReadyToSendData() {
        return currentState == ClientHandlerState.FORWARDING && clientKey.isWritable();
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


    @Override
    public void close() throws IOException {
        socketChannel.close();
        if (serverKey != null) {
            serverKey.channel().close();
        }
    }

    private void connect(@NotNull InetAddress address, int port) {
        PortValidator.validate(port);
        try {
            SocketChannel serverChannel = SocketChannel.open();
            serverChannel.configureBlocking(false);
            serverKey = serverChannel.register(clientKey.selector(), SelectionKey.OP_CONNECT);
            serverKey.attach(new ServerHandler(serverKey, clientKey));
            serverChannel.connect(new InetSocketAddress(address, port));
        } catch (IOException e) {
            logger.error("Problem with connection at address={}", address);
            throw new IllegalStateException("Problem with connection at address=" + address);
        }
    }

    public void connect(@NotNull InetAddress address) {
        Objects.requireNonNull(address, "Address cant be null");
        connect(address, portToConnect);
    }

    public void createResponse(@NotNull SOCKSErrorCode errorCode) {
        Objects.requireNonNull(errorCode, "Error code cant be null");
        buffer.clear();
        buffer.put(VERSION);
        buffer.put(errorCode.getCodeAsByte());
        buffer.put(RESERVED_BYTE);
        buffer.put(IPV4);
        for (int i = 0; i < 6; i++) {
            buffer.put(RESERVED_BYTE);
        }
        buffer.flip();
        currentState = getStateByErrorCode(errorCode);
        clientKey.interestOps(SelectionKey.OP_WRITE);
    }

    @NotNull
    private ClientHandlerState getStateByErrorCode(@NotNull SOCKSErrorCode errorCode) {
        return (errorCode == SOCKSErrorCode.SUCCESS)
                ? ClientHandlerState.SEND_CLIENT_RESPONSE
                : ClientHandlerState.SEND_CLIENT_ERROR;
    }
}