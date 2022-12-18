package ru.nsu.ccfit.korneshchuk.snakes.net;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class Neighbor extends NetNode {
    @NotNull
    private Instant lastSeenTime;

    public Neighbor(@NotNull NetNode netNode) {
        super(netNode.getAddress(), netNode.getPort());
        this.lastSeenTime = Instant.now();
    }

    @NotNull
    public Instant getLastSeenTime() {
        return lastSeenTime;
    }

    public void updateLastSeenTime() {
        lastSeenTime = Instant.now();
    }
}
