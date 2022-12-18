package ru.nsu.ccfit.korneshchuk.socks;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.attachment.AcceptHandler;
import ru.nsu.ccfit.korneshchuk.attachment.Attachment;
import ru.nsu.ccfit.korneshchuk.attachment.ClientHandler;
import ru.nsu.ccfit.korneshchuk.attachment.ServerHandler;
import ru.nsu.ccfit.korneshchuk.dns.DNSQueueOperationsWithExceptionsStrategy;
import ru.nsu.ccfit.korneshchuk.dns.DNSResolver;
import ru.nsu.ccfit.korneshchuk.utils.PortValidator;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class SOCKSProxy implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(SOCKSProxy.class);
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final DNSResolver dnsResolver;

    public SOCKSProxy(int port) {
        PortValidator.validate(port);
        try {
            SocketAddress socketAddress = getSocketAddress(port);
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(socketAddress);
            selector = SelectorProvider.provider().openSelector();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, serverSocketChannel.validOps(), new AcceptHandler(serverSocketChannel));
            dnsResolver = new DNSResolver(selector);
            dnsResolver.setDnsQueueStrategy(new DNSQueueOperationsWithExceptionsStrategy());
        } catch (UnknownHostException e) {
            logger.error("Error while get socket address on port={}", port, e);
            closeWithoutException();
            throw new IllegalStateException("Cant get socket address on port=" + port);
        } catch (ClosedChannelException e) {
            logger.error("Some operation on closed channel", e);
            closeWithoutException();
            throw new IllegalStateException("Some operation on closed channel", e);
        } catch (IOException e) {
            logger.error("Problem with create resources", e);
            closeWithoutException();
            throw new IllegalStateException("Problem with create resources", e);
        }
    }

    private void closeWithoutException() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (dnsResolver != null) {
                dnsResolver.close();
            }
        } catch (IOException e) {
            logger.error("Exception while closing resources", e);
        }
    }


    @NotNull
    private SocketAddress getSocketAddress(int port) throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByName("localhost"), port);
    }

    public void start() {
        try {
            while (selector.select() > -1) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                selectedKeysHandle(selectedKeys);

            }
        } catch (IOException ex) {
            logger.error("IO exception at select", ex);
            throw new IllegalStateException("IO exception at select", ex);
        }
    }

    private void selectedKeysHandle(Set<SelectionKey> selectedKeys) {
        selectedKeys.stream()
                .filter(SelectionKey::isValid)
                .forEach(key -> handleAttachment(key.attachment()));
        selectedKeys.clear();
    }

    private void handleAttachment(@NotNull Object attachmentObj) {
        if (!(attachmentObj instanceof Attachment)) {
            logger.error("Argument object={} is not attachment", attachmentObj);
            throw new IllegalArgumentException("Argument is not attachment");
        }
        Attachment attachment = (Attachment) attachmentObj;
        switch (attachment.getType()) {
            case ACCEPT_HANDLER -> handle((AcceptHandler) attachment);
            case DNS_RESOLVER -> handle((DNSResolver) attachment);
            case CLIENT_HANDLER -> handle((ClientHandler) attachment);
            case SERVER_HANDLER -> handle((ServerHandler) attachment);
            default -> {
                logger.error("Unknown attachment type={}", attachment.getType());
                throw new IllegalStateException("Unknown attachment type=" + attachment.getType());
            }
        }
    }

    private void handle(@NotNull ServerHandler serverHandler) {
        try {
            workWithServerHandler(serverHandler);
        } catch (Exception e) {
            logger.error("Proxy catch exception at server handler", e);
            serverHandler.closeWithoutException();
        }
    }

    private void workWithServerHandler(@NotNull ServerHandler serverHandler) {
        if (!serverHandler.isConnected()) {
            serverHandler.connect();
        } else if (serverHandler.isReadable()) {
            serverHandler.getDataFromServer();
        } else if (serverHandler.isWritable()) {
            serverHandler.sendDataToServer();
        }
    }

    private void handle(@NotNull AcceptHandler acceptHandler) {
        try {
            SocketChannel socketChannel = acceptHandler.accept();
            SelectionKey newKey = socketChannel.register(selector, SelectionKey.OP_READ);
            newKey.attach(new ClientHandler(dnsResolver, newKey));
        } catch (ClosedChannelException e) {
            logger.error("Operation with closed channel", e);
            throw new IllegalStateException("Operation with closed channel", e);
        }
    }

    private void handle(@NotNull DNSResolver resolver) {
        try {
            if (resolver.isReadable()) {
                resolver.sendRequest();
            } else if (resolver.isWritable()) {
                resolver.receiveRequest();
            }
        } catch (IllegalStateException e) {
            logger.error("Proxy catch exception at DNS resolver", e);
            resolver.closeWithoutException();
        }
    }

    private void handle(@NotNull ClientHandler clientHandler) {
        try {
            workWithClientHandler(clientHandler);
        } catch (UncheckedIOException | IllegalStateException e) {
            logger.error("Proxy catch exception at client handler", e);
            clientHandler.closeWithoutException();
            return;
        }
        clientHandler.changeState();
    }

    private void workWithClientHandler(@NotNull ClientHandler clientHandler) {
        if (clientHandler.isWaiting()) {
            clientHandler.read();
        } else if (clientHandler.isSending()) {
            clientHandler.write();
        } else if (clientHandler.isReadyToGetData()) {
            clientHandler.getDataFromClient();
        } else if (clientHandler.isReadyToSendData()) {
            clientHandler.sendDataToClient();
        }
    }

    @Override
    public void close() throws IOException {
        serverSocketChannel.close();
        dnsResolver.close();
    }
}
