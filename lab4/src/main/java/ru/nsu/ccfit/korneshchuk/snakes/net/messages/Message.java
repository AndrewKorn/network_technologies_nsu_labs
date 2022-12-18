package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class Message implements Serializable {
    private final UUID uuid;
    private final MessageType type;

    public Message(@NotNull MessageType type) {
        this.uuid = UUID.randomUUID();
        this.type = Objects.requireNonNull(type);
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public MessageType getType() {
        return type;
    }
}
