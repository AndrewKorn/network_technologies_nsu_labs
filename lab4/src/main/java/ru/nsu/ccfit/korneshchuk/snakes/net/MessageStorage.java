package ru.nsu.ccfit.korneshchuk.snakes.net;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.AckMessage;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.Message;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.MessageType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MessageStorage {
    private final Map<Message, NetNode> receivedMessages = new ConcurrentHashMap<>();
    private final Map<Message, NetNode> sentMessages = new ConcurrentHashMap<>();
    private final Map<Message, NetNode> messagesToSend = new ConcurrentHashMap<>();

    private void removeAllReceivedMessages() {
        receivedMessages.clear();
    }

    public void addMessageToSend(@NotNull NetNode receiver, @NotNull Message gameMessage) {
        messagesToSend.put(
                Objects.requireNonNull(gameMessage),
                Objects.requireNonNull(receiver)
        );
    }

    public void addReceivedMessage(@NotNull NetNode sender, @NotNull Message gameMessage) {
        if (gameMessage.getType() == MessageType.ACK) {
            removeConfirmedMessage((AckMessage) gameMessage);
            return;
        }
        receivedMessages.put(
                Objects.requireNonNull(gameMessage),
                Objects.requireNonNull(sender)
        );
    }

    private void removeConfirmedMessage(@NotNull AckMessage ackMessage) {
        sentMessages.keySet().removeIf(message ->
                message.getUuid()
                        .equals(ackMessage.getConfirmedMessageUUID())
        );
    }

    @NotNull
    public Map<Message, NetNode> getMessagesToSend() {
        Map<Message, NetNode> messages = Map.copyOf(messagesToSend);
        messagesToSend.clear();
        return messages;
    }

    @NotNull
    public Map<Message, NetNode> getReceivedMessages() {
        Map<Message, NetNode> messages = Map.copyOf(receivedMessages);
        removeAllReceivedMessages();
        return messages;
    }

    public void resendUnconfirmedMessages() {
        messagesToSend.putAll(sentMessages);
        sentMessages.clear();
    }
}
