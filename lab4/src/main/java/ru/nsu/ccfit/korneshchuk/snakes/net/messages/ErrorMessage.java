package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class ErrorMessage extends Message {
    @NotNull
    private final byte[] utf8Buffer;
    private transient String errorMessage;

    public ErrorMessage(@NotNull String errorMessage) {
        super(MessageType.ERROR);
        this.utf8Buffer = StandardCharsets.UTF_8.encode(errorMessage).array();
    }

    @NotNull
    public String getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new String(utf8Buffer, StandardCharsets.UTF_8);
        }
        return errorMessage;
    }
}
