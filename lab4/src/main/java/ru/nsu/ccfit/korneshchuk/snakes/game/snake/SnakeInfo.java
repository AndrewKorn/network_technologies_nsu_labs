package ru.nsu.ccfit.korneshchuk.snakes.game.snake;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.Player;
import ru.nsu.ccfit.korneshchuk.snakes.game.Direction;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SnakeInfo implements Serializable {
    @NotNull
    private final List<Point> snakePoints;

    @NotNull
    private final Direction direction;

    @Nullable
    private Player player;

    public SnakeInfo(@NotNull Snake snake) {
        player = null;
        snakePoints = List.copyOf(snake.getSnakePoints());
        direction = snake.getCurrentDirection();
    }

    public void setPlayer(@NotNull Player player) {
        this.player = Objects.requireNonNull(player, "Player cant be null");
    }

    @NotNull
    public List<Point> getSnakePoints() {
        return snakePoints;
    }

    @NotNull
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    public boolean isAliveSnake() {
        return player != null;
    }

    public boolean isZombieSnake() {
        return !isAliveSnake();
    }

    @NotNull
    public Point getSnakeHead() {
        return snakePoints.get(0);
    }

    @NotNull
    public Point getSnakeTail() {
        return snakePoints.get(snakePoints.size() - 1);
    }

    @NotNull
    public Direction getDirection() {
        return direction;
    }
}
