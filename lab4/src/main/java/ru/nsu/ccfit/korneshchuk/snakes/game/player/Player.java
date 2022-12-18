package ru.nsu.ccfit.korneshchuk.snakes.game.player;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {
    @NotNull
    private final String name;

    private Player(@NotNull String name) {
        this.name = name;
    }

    public static Player create(@NotNull String name) {
        return new Player(Objects.requireNonNull(name));
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return name.equals(player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
