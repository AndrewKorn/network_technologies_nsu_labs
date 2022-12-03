package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

public enum MessageType {
    STATE(true),
    ACK(false),
    PING(false),
    ROLE_CHANGE(true),
    STEER(true),
    ANNOUNCEMENT(false),
    JOIN(true),
    ERROR(false);


    final boolean needConfirmation;

    MessageType(boolean needConfirmation) {
        this.needConfirmation = needConfirmation;
    }

    public boolean isNeedConfirmation() {
        return needConfirmation;
    }
}
