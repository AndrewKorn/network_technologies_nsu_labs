package ru.nsu.ccfit.korneshchuk.snakes.game.cell;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Cell {
    @NotNull
    private final Point point;

    @NotNull
    private CellType type;


    public Cell(int x, int y, @NotNull CellType type) {
        this.point = new Point(x, y);
        this.type = Objects.requireNonNull(type, "Cell type cant be null");
    }

    public Cell(int x, int y) {
        this(x, y, CellType.EMPTY);
    }

    public Cell(@NotNull Cell cell) {
        this(cell.point, cell.getType());
    }

    public Cell(@NotNull Point point, @NotNull CellType type) {
        this.point = Objects.requireNonNull(point, "Point cant be null");
        this.type = Objects.requireNonNull(type, "Type cant be null");
    }

    @NotNull
    public CellType getType() {
        return type;
    }

    public int getX() {
        return point.x();
    }

    public int getY() {
        return point.y();
    }

    @NotNull
    public Point asPoint(){
        return point;
    }

    public void setType(@NotNull CellType type){
        this.type = Objects.requireNonNull(type, "Cell type cant be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return point.equals(cell.point) &&
                type == cell.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, type);
    }

}
