package ru.nsu.ccfit.korneshchuk.snakes.game;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Cell;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.CellType;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;

import java.util.*;


public class GameField {
    private static final int MAX_FIELD_WIDTH = 100;
    private static final int MAX_FIELD_HEIGHT = 100;

    private final List<Cell> field;
    private final List<Cell> emptyCells;
    private final int width;
    private final int height;
    private final Random random;

    public GameField(int width, int height) {
        validateFieldSizes(width, height);
        field = new ArrayList<>(width * height);
        emptyCells = new ArrayList<>(width * height);
        for (int row = 0; row < height; ++row){
            for (int col = 0; col < width; ++col){
                Cell cell = new Cell(col, row);
                field.add(cell);
                emptyCells.add(cell);
            }
        }
        this.width = width;
        this.height = height;
        random = new Random();
    }

    public GameField(int size) {
        this(size, size);
    }

    private void validateFieldSizes(int width, int height){
        if (width <= 0 || width > MAX_FIELD_WIDTH){
            throw new IllegalArgumentException("Width not from valid interval: ["
                    + 1 + ", " + MAX_FIELD_WIDTH + "]");
        }
        if (height <= 0 || height > MAX_FIELD_HEIGHT){
            throw new IllegalArgumentException("Height not from valid interval: ["
                    + 1 + ", " + MAX_FIELD_HEIGHT + "]");
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @NotNull
    public Cell get(int row, int col){
        return new Cell(accessToCell(row, col));
    }

    private Cell accessToCell(int row, int col){
        int y = (row < 0) ? height + row : row % height;
        int x = (col < 0) ? width + col : col % width;
        return field.get(y * width + x);
    }

    public void set(int row, int col, @NotNull CellType type){
        Cell cell = accessToCell(row, col);
        if (type == CellType.EMPTY){
            if (cell.getType() != CellType.EMPTY){  //equal to emptyCells.contains(cell)
                emptyCells.add(cell);
            }
        } else{
            emptyCells.remove(cell);
        }
        cell.setType(Objects.requireNonNull(type, "Type cant be null"));
    }

    public void set(@NotNull Point point, @NotNull CellType type) {
        Objects.requireNonNull(point, "Point cant be null");
        set(point.getY(), point.getX(), type);
    }

    public int getEmptyCellsNumber() {
        return emptyCells.size();
    }

    Optional<Cell> findCenterOfSquareWithOutSnake(int squareSize) {
        return field.stream()
                .filter(cell -> isSquareWithoutSnake(cell, squareSize))
                .findFirst();
    }

    private boolean isSquareWithoutSnake(Cell squareCenter, int squareSize) {
        final int centerOffset = squareSize / 2;
        for (int yCenterOffset = -centerOffset; yCenterOffset <= centerOffset; yCenterOffset++) {
            for (int xCenterOffset = -centerOffset; xCenterOffset <= centerOffset; xCenterOffset++) {
                Cell cell = accessToCell(
                        squareCenter.getY() + yCenterOffset,
                        squareCenter.getX() + xCenterOffset
                );
                if (cell.getType() == CellType.SNAKE) {
                    return false;
                }
            }
        }
        return true;
    }

    @NotNull
    public Optional<Cell> findRandomEmptyCell(){
        if (emptyCells.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(
                emptyCells.get(
                        random.nextInt(emptyCells.size() - 1)
                )
        );
    }
}
