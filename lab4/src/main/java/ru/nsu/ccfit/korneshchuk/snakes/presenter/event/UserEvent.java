package ru.nsu.ccfit.korneshchuk.snakes.presenter.event;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class UserEvent {
    @NotNull
    private final UserEventType type;

    public UserEvent(@NotNull UserEventType type) {
        this.type = Objects.requireNonNull(type, "Type cant be null");
    }

    @NotNull
    public UserEventType getType() {
        return type;
    }
}
