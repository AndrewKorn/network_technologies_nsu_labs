package ru.nsu.ccfit.korneshchuk.snakes.net.node;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.config.Config;

import java.util.Objects;

public final class NodeFactory {
    private NodeFactory() {
    }

    @NotNull
    public static GameNode createNode(@NotNull Role role, @NotNull Config config) {
        Objects.requireNonNull(role, "Role cant be null");
        Objects.requireNonNull(config, "Config cant be null");
        return switch (role) {
            case MASTER -> new MasterNode(config);
            case DEPUTY -> new DeputyNode(config);
            case NORMAL -> new NormalNode(config);
            default -> throw new IllegalArgumentException("Unknown role");
        };
    }
}
