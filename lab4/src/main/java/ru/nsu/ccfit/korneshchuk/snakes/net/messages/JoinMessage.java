package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;


public class JoinMessage extends Message {
    private final byte[] utf8Buffer;
    private transient String playerName;

    public JoinMessage(@NotNull String playerName) {
        super(MessageType.JOIN);
        this.utf8Buffer = StandardCharsets.UTF_8.encode(playerName).array();
    }

    @NotNull
    public String getPlayerName() {
        if (playerName == null) {
            playerName = new String(utf8Buffer, StandardCharsets.UTF_8);
        }
        return playerName;
    }
}
