package ru.nsu.ccfit.korneshchuk.snakes.net.messagehandler;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.net.NetNode;
import ru.nsu.ccfit.korneshchuk.snakes.net.messages.AnnouncementMessage;


public interface AnnouncementMessageHandler {
    void handle(@NotNull NetNode sender, @NotNull AnnouncementMessage announcementMsg);
}
