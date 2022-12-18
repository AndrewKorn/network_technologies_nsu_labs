package ru.nsu.ccfit.korneshchuk.snakes.net.messagehandler;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.net.NetNode;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.RoleChangeMessage;

public interface RoleChangeMessageHandler {
    void handle(@NotNull NetNode sender, @NotNull RoleChangeMessage roleChangeMsg);
}
