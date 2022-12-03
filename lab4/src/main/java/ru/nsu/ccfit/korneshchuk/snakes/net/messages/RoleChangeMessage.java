package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.net.node.Role;

import java.util.Objects;

public class RoleChangeMessage extends Message {
    private final Role roleFrom;
    private final Role roleTo;

    public RoleChangeMessage(@NotNull Role roleFrom, @NotNull Role roleTo) {
        super(MessageType.ROLE_CHANGE);
        this.roleFrom = Objects.requireNonNull(roleFrom, "Role from cant be null");
        this.roleTo = Objects.requireNonNull(roleTo, "Role to cant be null");
    }

    @NotNull
    public Role getFromRole() {
        return roleFrom;
    }

    @NotNull
    public Role getToRole() {
        return roleTo;
    }
}
