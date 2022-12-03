package ru.nsu.ccfit.korneshchuk.snakes.game.cell;

import java.io.Serializable;

public record Point(int x, int y) implements Serializable {

    @Override
    public boolean equals(Object p) {
        if (this == p) {
            return true;
        }
        if (!(p instanceof Point)) {
            return false;
        }
        Point tmp = (Point) p;
        return x == tmp.x && y == tmp.y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + x;
        hash = 71 * hash + y;
        return hash;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}
