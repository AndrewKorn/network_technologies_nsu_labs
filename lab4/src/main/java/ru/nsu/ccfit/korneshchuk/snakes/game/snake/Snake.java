package ru.nsu.ccfit.korneshchuk.snakes.game.snake;


import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.game.Direction;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.PointUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Snake implements Iterable<Point> {
    private final int xCoordinateLimit;
    private final int yCoordinateLimit;

    @NotNull
    private Point head;

    @NotNull
    private Point tail;

    @NotNull
    private Direction currentDir;

    @NotNull
    private final List<Point> snakePoints;

    public Snake(@NotNull Point head,
                 @NotNull Point tail,
                 int xCoordinateLimit,
                 int yCoordinateLimit) {
        this.xCoordinateLimit = xCoordinateLimit;
        this.yCoordinateLimit = yCoordinateLimit;
        this.head = Objects.requireNonNull(head, "Head point cant be null");
        this.tail = Objects.requireNonNull(tail, "Tail point cant be null");
        validateInitHeadAndTail(head, tail);
        snakePoints = new ArrayList<>();
        snakePoints.add(head);
        snakePoints.add(tail);

        this.currentDir = calculateCurrentDirection(head, tail);
    }

    public Snake(@NotNull List<Point> points,
                 @NotNull Direction currentDir,
                 int xCoordinateLimit,
                 int yCoordinateLimit) {
        this.xCoordinateLimit = xCoordinateLimit;
        this.yCoordinateLimit = yCoordinateLimit;
        this.currentDir = Objects.requireNonNull(currentDir, "Direction cant be null");
        snakePoints = new ArrayList<>(points.size());
        snakePoints.addAll(points);
        head = snakePoints.get(0);
        tail = snakePoints.get(snakePoints.size() - 1);

    }

    private void validateInitHeadAndTail(Point head, Point tail) {
        if (!PointUtils.arePointsStraightConnected(head, tail, xCoordinateLimit, yCoordinateLimit)) {
            throw new IllegalArgumentException("Head and tail are not connected");
        }
    }

    private Direction calculateCurrentDirection(Point head, Point tail) {
        validateInitHeadAndTail(head, tail);
        if (PointUtils.getPointToRight(head, xCoordinateLimit).equals(tail)) {
            return Direction.LEFT;
        } else if (PointUtils.getPointToLeft(head, xCoordinateLimit).equals(tail)) {
            return Direction.RIGHT;
        } else if (PointUtils.getPointBelow(head, yCoordinateLimit).equals(tail)) {
            return Direction.UP;
        } else if (PointUtils.getPointAbove(head, yCoordinateLimit).equals(tail)) {
            return Direction.DOWN;
        }
        throw new IllegalStateException("Cant calculate current direction");
    }

    public void makeMove(@NotNull Direction dir) {
        Objects.requireNonNull(dir, "Direction cant be null");
        if (dir.getReversed() == currentDir) {
            dir = currentDir;  //Блокирует движение змейки в противоположном направлении
        }
        currentDir = dir;
        head = getNewHead(dir);
        snakePoints.add(0, head);
    }

    private Point getNewHead(@NotNull Direction dir) {
        return switch (dir) {
            case DOWN -> PointUtils.getPointBelow(head, yCoordinateLimit);
            case UP -> PointUtils.getPointAbove(head, yCoordinateLimit);
            case LEFT -> PointUtils.getPointToLeft(head, xCoordinateLimit);
            case RIGHT -> PointUtils.getPointToRight(head, xCoordinateLimit);
            default -> throw new IllegalStateException("Unknown direction = " + dir);
        };
    }

    public void makeMove() {
        makeMove(currentDir);
    }

    public int getSnakeSize() {
        return snakePoints.size();
    }

    @NotNull
    public Point getHead() {
        return head;
    }

    @NotNull
    public Point getTail() {
        return tail;
    }

    public void removeTail() {
        snakePoints.remove(tail);
        if (snakePoints.size() <= 1) {
            throw new IllegalStateException("Snake cant have less than 2 points");
        }
        tail = snakePoints.get(snakePoints.size() - 1);
    }

    /**
     * @param p - point to check
     * @return true if p is snake body, return false if p is head or tail
     */
    public boolean isSnakeBody(@NotNull Point p) {

        for (int i = 1; i < snakePoints.size() - 1; ++i) {
            if (p.equals(snakePoints.get(i))) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    Direction getCurrentDirection() {
        return currentDir;
    }

    @NotNull
    List<Point> getSnakePoints() {
        return snakePoints;
    }

    public boolean isSnake(@NotNull Point p) {
        return p.equals(head) || p.equals(tail) || isSnakeBody(p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snake points = (Snake) o;
        return snakePoints.equals(points.snakePoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snakePoints);
    }

    /**
     * Returns an iterator over elements of Point.
     *
     * @return an Iterator of snake.
     */
    @NotNull
    @Override
    public Iterator<Point> iterator() {
        return snakePoints.iterator();
    }
}
