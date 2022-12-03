package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class AckMessage extends Message {
    @NotNull
    private final UUID confirmedMessageUUID;

    public AckMessage(@NotNull UUID confirmedMessageUUID) {
        super(MessageType.ACK);
        this.confirmedMessageUUID = Objects.requireNonNull(
                confirmedMessageUUID,
                "Confirmed message uuid cant be null"
        );
    }

    @NotNull
    public UUID getConfirmedMessageUUID() {
        return confirmedMessageUUID;
    }
}
