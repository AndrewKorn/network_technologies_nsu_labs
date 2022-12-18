package ru.nsu.ccfit.korneshchuk.snakes.game.cell;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class PointUtils {
    private static final String POINT_NULL_ERROR_MESSAGE = "Point cant be null";

    private PointUtils() {
    }

    private static int countNewPointCoordinate(int newCoordinate, int coordinateLimit) {
        if (newCoordinate >= coordinateLimit) {
            return newCoordinate % coordinateLimit;
        } else if (newCoordinate < 0) {
            return coordinateLimit - 1;
        }
        return newCoordinate;
    }

    @NotNull
    public static Point getPointAbove(@NotNull Point point) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(point.x(), point.y() - 1);
    }

    @NotNull
    public static Point getPointBelow(@NotNull Point point) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(point.x(), point.y() + 1);
    }

    @NotNull
    public static Point getPointToRight(@NotNull Point point) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(point.x() + 1, point.y());
    }

    @NotNull
    public static Point getPointToLeft(@NotNull Point point) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(point.x() - 1, point.y());
    }

    @NotNull
    public static Point getPointAbove(@NotNull Point point, int yLimit) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(
                point.x(),
                countNewPointCoordinate(point.y() - 1, yLimit)
        );
    }

    @NotNull
    public static Point getPointBelow(@NotNull Point point, int yLimit) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(
                point.x(),
                countNewPointCoordinate(point.y() + 1, yLimit)
        );
    }

    @NotNull
    public static Point getPointToRight(@NotNull Point point, int xLimit) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(
                countNewPointCoordinate(point.x() + 1, xLimit),
                point.y()
        );
    }

    @NotNull
    public static Point getPointToLeft(@NotNull Point point, int xLimit) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return new Point(
                countNewPointCoordinate(point.x() - 1, xLimit),
                point.y()
        );
    }

    @NotNull
    public static List<Point> getStraightConnectedPoints(@NotNull Point point, int xLimit, int yLimit) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return List.of(
                PointUtils.getPointAbove(point, yLimit),
                PointUtils.getPointBelow(point, yLimit),
                PointUtils.getPointToLeft(point, xLimit),
                PointUtils.getPointToRight(point, xLimit)
        );
    }

    @NotNull
    public static List<Point> getStraightConnectedPoints(@NotNull Point point) {
        Objects.requireNonNull(point, POINT_NULL_ERROR_MESSAGE);
        return List.of(
                PointUtils.getPointAbove(point),
                PointUtils.getPointBelow(point),
                PointUtils.getPointToLeft(point),
                PointUtils.getPointToRight(point)
        );
    }

    public static boolean arePointsStraightConnected(@NotNull Point p1, @NotNull Point p2) {
        Objects.requireNonNull(p1, POINT_NULL_ERROR_MESSAGE);
        Objects.requireNonNull(p1, POINT_NULL_ERROR_MESSAGE);
        return getStraightConnectedPoints(p1).contains(p2);
    }

    public static boolean arePointsStraightConnected(@NotNull Point p1, @NotNull Point p2, int xLimit, int yLimit) {
        Objects.requireNonNull(p1, POINT_NULL_ERROR_MESSAGE);
        Objects.requireNonNull(p1, POINT_NULL_ERROR_MESSAGE);
        return getStraightConnectedPoints(p1, xLimit, yLimit).contains(p2);
    }
}
