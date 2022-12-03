package ru.nsu.ccfit.korneshchuk.snakes.game;

import org.jetbrains.annotations.NotNull;

public enum Direction {
    DOWN,
    UP,
    RIGHT,
    LEFT;

    @NotNull
    public Direction getReversed(){
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
        };
    }
}
