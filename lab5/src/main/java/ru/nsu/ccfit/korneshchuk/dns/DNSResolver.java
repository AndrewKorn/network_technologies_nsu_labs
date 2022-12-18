package ru.nsu.ccfit.korneshchuk.dns;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import ru.nsu.ccfit.korneshchuk.attachment.Attachment;
import ru.nsu.ccfit.korneshchuk.attachment.AttachmentType;
import ru.nsu.ccfit.korneshchuk.attachment.ClientHandler;
import ru.nsu.ccfit.korneshchuk.socks.SOCKSErrorCode;
import ru.nsu.ccfit.korneshchuk.utils.SelectionKeyUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DNSResolver extends Attachment implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(DNSResolver.class);
    private static final int DNS_DEFAULT_PORT = 53;
    private static final int QUEUE_CAPACITY = 100;
    private static final int RECEIVER_BUFFER_CAPACITY = 1024;
    private static final Random random = new Random();

    private final DatagramChannel resolverSocket;
    private final InetSocketAddress dnsAddress;
    private final BlockingQueue<DNSResolveRequest> requestQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private DNSQueueStrategy dnsQueueStrategy = new DNSQueueOperationsWithExceptionsStrategy();
    private final Map<Integer, DNSResolveRequest> sentRequests = new ConcurrentHashMap<>();
    private final SelectionKey resolverKey;
    private final ByteBuffer receiverBuffer = ByteBuffer.allocate(RECEIVER_BUFFER_CAPACITY);

    public DNSResolver(@NotNull Selector selector) {
        super(AttachmentType.DNS_RESOLVER);
        Objects.requireNonNull(selector, "Selector cant be null");
        try {
            resolverSocket = DatagramChannel.open();
            resolverSocket.configureBlocking(false);
            resolverKey = resolverSocket.register(selector, 0);
            resolverKey.attach(this);
            resolverKey.interestOps(SelectionKey.OP_READ);
            dnsAddress = new InetSocketAddress(ResolverConfig.getCurrentConfig().server().getAddress(), DNS_DEFAULT_PORT);
        } catch (IOException e) {
            closeWithoutException();
            throw new IllegalStateException("Cant create DNSResolver");
        }
    }

    public void setDnsQueueStrategy(@NotNull DNSQueueStrategy dnsQueueStrategy) {
        this.dnsQueueStrategy = Objects.requireNonNull(dnsQueueStrategy, "Dns queue strategy cant be null");
    }

    public void appendResolveRequest(@NotNull String hostToResolve, @NotNull ClientHandler clientHandler) {
        Objects.requireNonNull(hostToResolve, "Host cant be null");
        Objects.requireNonNull(clientHandler, "Client handler cant be null");
        dnsQueueStrategy.appendResolveRequest(
                requestQueue,
                new DNSResolveRequest(hostToResolve, clientHandler)
        );
        SelectionKeyUtils.turnOnWriteOption(resolverKey);
    }


    public void closeWithoutException() {
        try {
            if (resolverSocket != null) {
                resolverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error while closing", e);
        }
    }

    public boolean isReadable() {
        return resolverKey.isReadable();
    }

    public boolean isWritable() {
        return resolverKey.isWritable();
    }

    @Override
    public void close() throws IOException {
        resolverSocket.close();
    }

    public void sendRequest() {
        if (requestQueue.isEmpty()) {
            SelectionKeyUtils.turnOffWriteOption(resolverKey);
            return;
        }
        DNSResolveRequest dnsResolveRequest = dnsQueueStrategy.takeResolveRequest(requestQueue);
        int key = random.nextInt();
        sentRequests.put(key, dnsResolveRequest);
        Message message = createMessage(dnsResolveRequest, key);
        try {
            resolverSocket.send(ByteBuffer.wrap(message.toWire()), dnsAddress);
        } catch (IOException e) {
            logger.error("Error while sending request={} to dns address={}", message, dnsAddress, e);
            throw new IllegalStateException("Cant send request=" + message + " to dns address=" + dnsAddress, e);
        }
        logger.debug("Request={} successfully sent to dns at address={}", message, dnsAddress);
    }

    @NotNull
    private Message createMessage(DNSResolveRequest dnsResolveRequest, int key) {
        Message message = new Message();
        Header header = message.getHeader();
        header.setID(key);
        header.setFlag(Flags.RD);
        header.setOpcode(Opcode.QUERY);
        String absoluteName = dnsResolveRequest.getHostToResolve() + ".";
        try {
            message.addRecord(Record.newRecord(new Name(absoluteName), Type.A, DClass.IN), Section.QUESTION);
        } catch (TextParseException e) {
            logger.error("Wrong name={}", absoluteName, e);
            throw new IllegalStateException("Wrong name=" + absoluteName, e);
        }
        return message;
    }

    public void receiveRequest() {
        Message message = readMessage();
        if (message.getRcode() != Rcode.NOERROR) {
            logger.error("Error code={} at received message", message.getRcode());
            return;
        }
        int messageID = message.getHeader().getID();
        DNSResolveRequest dnsResolveRequest = getSentResolveRequestByID(messageID);
        ClientHandler clientHandler = dnsResolveRequest.getClientHandler();
        Arrays.stream(message.getSectionArray(Section.ANSWER))
                .filter(record ->
                        record.getType() == Type.A
                )
                .findFirst()
                .ifPresentOrElse(record -> clientHandler.connect(((ARecord) record).getAddress()),
                        () -> clientHandler.createResponse(SOCKSErrorCode.DESTINATION_HOST_UNREACHABLE));

    }

    @NotNull
    private DNSResolveRequest getSentResolveRequestByID(int messageID) {
        if (!sentRequests.containsKey(messageID)) {
            logger.error("Get dns response not from sent requests with id={}", messageID);
            throw new IllegalStateException("Get dns response not from sent requests with id=" + messageID);
        }
        DNSResolveRequest dnsResolveRequest = sentRequests.get(messageID);
        sentRequests.remove(messageID);
        return dnsResolveRequest;
    }

    @NotNull
    private Message readMessage() {
        try {
            receiverBuffer.clear();
            resolverSocket.receive(receiverBuffer);
            receiverBuffer.flip();
            return new Message(receiverBuffer.array());
        } catch (IOException e) {
            logger.error("Cant receive message", e);
            throw new IllegalStateException("Cant receive message");
        }
    }
}