package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.config.Config;

import java.util.Objects;

public class AnnouncementMessage extends Message {
    @NotNull
    private final Config config;
    private final int playersNumber;
    private final boolean canJoin;

    public AnnouncementMessage(@NotNull Config config, int playersNumber, boolean canJoin) {
        super(MessageType.ANNOUNCEMENT);
        this.playersNumber = playersNumber;
        this.config = Objects.requireNonNull(config, "Config cant be null");
        this.canJoin = canJoin;
    }

    @NotNull
    public Config getConfig() {
        return config;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    public boolean canJoin() {
        return canJoin;
    }
}
