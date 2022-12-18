package ru.nsu.ccfit.korneshchuk.dns;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.attachment.ClientHandler;

public class DNSResolveRequest {
    @NotNull
    private final String hostToResolve;
    @NotNull
    private final ClientHandler clientHandler;

    public DNSResolveRequest(@NotNull String hostToResolve, @NotNull ClientHandler clientHandler) {
        this.hostToResolve = hostToResolve;
        this.clientHandler = clientHandler;
    }

    public @NotNull String getHostToResolve() {
        return this.hostToResolve;
    }

    public @NotNull ClientHandler getClientHandler() {
        return this.clientHandler;
    }
}