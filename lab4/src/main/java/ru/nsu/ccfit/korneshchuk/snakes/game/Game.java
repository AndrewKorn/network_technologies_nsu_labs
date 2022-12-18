package ru.nsu.ccfit.korneshchuk.snakes.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.snakes.config.GameConfig;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Cell;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.CellType;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.Player;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.PlayerWithScore;
import ru.nsu.ccfit.korneshchuk.snakes.game.snake.Snake;
import ru.nsu.ccfit.korneshchuk.snakes.game.snake.SnakeInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game implements GameObservable {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private static final int SIZE_OF_EMPTY_SQUARE_FOR_SNAKE = 5;
    public static final String UNKNOWN_PLAYER_ERROR_MESSAGE = "Unknown player";

    private final Map<Player, Snake> playersWithSnakes = new HashMap<>();
    private final Map<Player, Integer> playersScores = new HashMap<>();
    private final Map<Player, Snake> playersForRemove = new HashMap<>();
    private final List<Snake> zombieSnakes = new ArrayList<>();
    private final GameConfig config;
    private final List<Cell> fruits;
    private final GameField field;
    private final ArrayList<GameObserver> gameObservers;
    private final Random random = new Random();
    private int stateID;

    public Game(@NotNull GameConfig config) {
        this.config = Objects.requireNonNull(config, "Config cant be null");
        field = new GameField(config.getFieldWidth(), config.getFieldHeight());
        gameObservers = new ArrayList<>();
        stateID = 0;
        fruits = new ArrayList<>(config.getFoodStaticNumber());
        generateFruits();
    }

    public Game(@NotNull GameState state) {
        config = state.getGameConfig();
        field = new GameField(config.getFieldWidth(), config.getFieldHeight());
        stateID = state.getStateID();
        gameObservers = new ArrayList<>();
        List<SnakeInfo> snakeInfos = state.getSnakeInfos();
        snakeInfos.forEach(snakeInfo -> {
            Snake snake = createSnakeFromSnakeInfo(snakeInfo);
            markSnakeOnField(snake);
            if (snakeInfo.isZombieSnake()) {
                zombieSnakes.add(snake);
            } else {
                Player snakeOwner = snakeInfo.getPlayer()
                        .orElseThrow(
                                () -> new IllegalStateException("Cant get player from alive snake")
                        );
                playersWithSnakes.put(snakeOwner, snake);
            }
        });
        state.getActivePlayers().forEach(
                playerWithScore -> playersScores.put(
                        playerWithScore.getPlayer(),
                        playerWithScore.getScore()
                )
        );
        fruits = new ArrayList<>(state.getFruits().size());
        state.getFruits().forEach(fruit -> {
            field.set(fruit, CellType.FRUIT);
            fruits.add(new Cell(fruit, CellType.FRUIT));
        });
    }

    private void markSnakeOnField(Snake snake) {
        for (Point snakePoint : snake) {
            field.set(snakePoint, CellType.SNAKE);
        }
    }

    @NotNull
    private Snake createSnakeFromSnakeInfo(SnakeInfo snakeInfo) {
        return new Snake(
                snakeInfo.getSnakePoints(),
                snakeInfo.getDirection(),
                config.getFieldWidth(),
                config.getFieldHeight()
        );
    }

    @NotNull
    public Player registrationNewPlayer(@NotNull String playerName) {
        Player player = Player.create(playerName);
        List<Cell> headAndTailOfNewSnake = getNewSnakeHeadAndTail();
        if (headAndTailOfNewSnake.isEmpty()) {
            throw new IllegalStateException("Cant add new player because no space on field");
        }
        Snake playerSnake = new Snake(
                headAndTailOfNewSnake.get(0).asPoint(),
                headAndTailOfNewSnake.get(1).asPoint(),
                field.getWidth(),
                field.getHeight()
        );
        headAndTailOfNewSnake.forEach(cell -> field.set(cell.getY(), cell.getX(), CellType.SNAKE));
        playersWithSnakes.put(player, playerSnake);
        playersScores.put(player, 0);
        return player;
    }

    private List<Cell> getNewSnakeHeadAndTail() {
        Optional<Cell> centerOfEmptySquareOnField = field.findCenterOfSquareWithOutSnake(SIZE_OF_EMPTY_SQUARE_FOR_SNAKE);
        if (centerOfEmptySquareOnField.isEmpty()) {
            return Collections.emptyList();
        }
        Cell snakeHead = centerOfEmptySquareOnField.get();
        Optional<Cell> snakeTail = findTailWithoutFruit(snakeHead);
        if (snakeTail.isEmpty()) {
            return Collections.emptyList();
        }
        return List.of(snakeHead, snakeTail.get());
    }

    private Optional<Cell> findTailWithoutFruit(Cell head) {
        return Stream.of(
                        field.get(head.getY() - 1, head.getX()),
                        field.get(head.getY() + 1, head.getX()),
                        field.get(head.getY(), head.getX() - 1),
                        field.get(head.getY(), head.getX() + 1)
                )
                .filter(cell -> cell.getType() == CellType.EMPTY)
                .findFirst();
    }

    public void removePlayer(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cant be null");
        if (!playersWithSnakes.containsKey(player)) {
            return;
        }
        zombieSnakes.add(playersWithSnakes.get(player));
        markPlayerInactive(player);
    }

    private void markPlayerInactive(@NotNull Player player) {
        playersWithSnakes.remove(player);
        playersScores.remove(player);
    }

    private void makeMove(@NotNull Player player, @Nullable Direction direction) {
        Objects.requireNonNull(player, "Player cant be null");
        if (!playersWithSnakes.containsKey(player)) {
            throw new IllegalArgumentException(UNKNOWN_PLAYER_ERROR_MESSAGE);
        }
        Snake snake = playersWithSnakes.get(player);
        if (direction == null) {
            snake.makeMove();
        } else {
            snake.makeMove(direction);
        }
        if (isSnakeCrashed(playersWithSnakes.get(player))) {
            handlePlayerLose(player, snake);
            return;
        }
        if (isSnakeAteFruit(snake)) {
            incrementScore(player);
            removeFruit(snake.getHead());
        } else {
            field.set(snake.getTail(), CellType.EMPTY);
            snake.removeTail();
        }
        field.set(snake.getHead(), CellType.SNAKE);
    }

    private void removeFruit(Point fruitForRemove) {
        fruits.removeIf(fruit -> fruitForRemove.equals(fruit.asPoint()));
    }

    private void handlePlayerLose(Player player, Snake playerSnake) {
        playersForRemove.put(player, playerSnake);
    }

    public GameConfig getConfig() {
        return config;
    }

    public void makeAllPlayersMove(@NotNull Map<Player, Direction> playersMoves) {
        playersWithSnakes
                .keySet()
                .forEach(
                        player -> makeMove(player, playersMoves.getOrDefault(player, null))
                );
        zombieSnakesMove();
        generateFruits();
        playersForRemove
                .keySet()
                .forEach(player -> {
                    makeFruitsFromSnakeWithProbability(playersWithSnakes.get(player));
                    markPlayerInactive(player);
                });
        playersForRemove.clear();
        notifyObservers();
    }

    private void zombieSnakesMove() {
        zombieSnakes.forEach(this::zombieMove);
        zombieSnakes.stream()
                .filter(this::isSnakeCrashed)
                .forEach(this::makeFruitsFromSnakeWithProbability);
        zombieSnakes.removeIf(this::isSnakeCrashed);
    }

    private void zombieMove(Snake snake) {
        snake.makeMove();
        if (isSnakeAteFruit(snake)) {
            removeFruit(snake.getHead());
        } else {
            field.set(snake.getTail(), CellType.EMPTY);
            snake.removeTail();
        }
        field.set(snake.getHead(), CellType.SNAKE);
    }

    private void generateFruits() {
        int aliveSnakesCount = playersWithSnakes.size();
        int requiredFruitsNumber = config.getFoodStaticNumber() + config.getFoodPerPlayer() * aliveSnakesCount;
        if (fruits.size() == requiredFruitsNumber) {
            return;
        }
        if (field.getEmptyCellsNumber() < requiredFruitsNumber) {
            logger.debug("Cant generate required number of fruits={}, empty cells number={}",
                    requiredFruitsNumber,
                    field.getEmptyCellsNumber()
            );
            return;
        }
        while (fruits.size() < requiredFruitsNumber) {
            Cell randomEmptyCell = field.findRandomEmptyCell()
                    .orElseThrow(() -> new IllegalStateException("Cant find empty cell"));
            field.set(randomEmptyCell.asPoint(), CellType.FRUIT);
            fruits.add(randomEmptyCell);
        }
    }

    private void incrementScore(Player player) {
        if (!playersScores.containsKey(player)) {
            throw new IllegalArgumentException(UNKNOWN_PLAYER_ERROR_MESSAGE);
        }
        int prevScore = playersScores.get(player);
        playersScores.put(player, prevScore + 1);

    }

    private boolean isSnakeAteFruit(Snake snake) {
        Point snakeHead = snake.getHead();
        return fruits.stream()
                .anyMatch(
                        fruit -> snakeHead.equals(fruit.asPoint())
                );
    }

    private void makeFruitsFromSnakeWithProbability(Snake snake) {
        for (Point p : snake) {
            if (p.equals(snake.getHead())) {
                continue;
            }
            if (random.nextDouble() < config.getProbabilityOfDeadSnakeCellsToFood()) {
                field.set(p, CellType.FRUIT);
                fruits.add(field.get(p.getY(), p.getX()));
            } else {
                field.set(p, CellType.EMPTY);
            }
        }
    }

    private boolean isSnakeCrashed(Snake snake) {
        if (isSnakeCrashedToZombie(snake)) {
            return true;
        }
        for (Map.Entry<Player, Snake> playerWithSnake : playersWithSnakes.entrySet()) {
            Snake otherSnake = playerWithSnake.getValue();
            if (checkCrashIntoYourself(snake)) {
                return true;
            }
            if (otherSnake != snake && otherSnake.isSnake(snake.getHead())) {
                incrementScore(playerWithSnake.getKey());
                return true;
            }
        }
        return false;
    }

    private boolean isSnakeCrashedToZombie(Snake snake) {
        return zombieSnakes.stream()
                .anyMatch(zombieSnake ->
                        zombieSnake != snake && zombieSnake.isSnake(snake.getHead())
                );
    }

    private boolean checkCrashIntoYourself(Snake snake) {
        return snake.isSnakeBody(snake.getHead()) || snake.getTail().equals(snake.getHead());
    }

    @Override
    public void addObserver(GameObserver gameObserver) {
        gameObservers.add(gameObserver);
    }

    @Override
    public void removeObserver(GameObserver gameObserver) {
        gameObservers.remove(gameObserver);
    }


    @Override
    public void notifyObservers() {
        GameState gameState = generateGameState();
        for (GameObserver gameObserver : gameObservers) {
            gameObserver.update(gameState);
        }
    }

    private GameState generateGameState() {
        int currentStateID = this.stateID++;
        return new GameState(
                getFruitsAsPointsList(),
                generatePlayersWithTheirScoresList(),
                generateSnakeInfosList(),
                config,
                currentStateID
        );
    }

    @NotNull
    private List<Point> getFruitsAsPointsList() {
        return fruits.stream()
                .map(Cell::asPoint)
                .collect(Collectors.toList());
    }

    @NotNull
    private List<SnakeInfo> generateSnakeInfosList() {
        List<SnakeInfo> snakeInfos = new ArrayList<>(playersWithSnakes.size() + zombieSnakes.size());
        playersWithSnakes.forEach((player, snake) -> {
            SnakeInfo snakeInfo = new SnakeInfo(snake);
            snakeInfo.setPlayer(player);
            snakeInfos.add(snakeInfo);
        });
        zombieSnakes.forEach(snake -> snakeInfos.add(new SnakeInfo(snake)));
        return snakeInfos;
    }

    @NotNull
    private List<PlayerWithScore> generatePlayersWithTheirScoresList() {
        List<PlayerWithScore> playerWithScores = new ArrayList<>(playersScores.size());
        playersScores.forEach((player, score) -> playerWithScores.add(new PlayerWithScore(player, score)));
        return playerWithScores;
    }
}
