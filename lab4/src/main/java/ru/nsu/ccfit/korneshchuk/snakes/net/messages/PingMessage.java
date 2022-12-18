package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

public class PingMessage extends Message {
    public PingMessage() {
        super(MessageType.PING);
    }
}
