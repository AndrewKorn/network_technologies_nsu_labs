package ru.nsu.ccfit.korneshchuk.snakes.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.config.GameConfig;
import ru.nsu.ccfit.korneshchuk.snakes.game.Direction;
import ru.nsu.ccfit.korneshchuk.snakes.game.cell.Point;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.PlayerWithScore;
import ru.nsu.ccfit.korneshchuk.snakes.gui.utils.GameInfoWithButton;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.GamePresenter;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.ExitEvent;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.JoinToGameEvent;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.MoveEvent;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.NewGameEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class GameViewController implements View {

    private static final Paint FRUIT_COLOR = Color.GREEN;
    private static final Paint EMPTY_CELL_COLOR = Color.WHITE;
    @FXML
    private TableColumn<GameInfoWithButton, String> masterColumn;
    @FXML
    private TableColumn<GameInfoWithButton, Integer> playersNumberColumn;
    @FXML
    private TableColumn<GameInfoWithButton, String> fieldSizeColumn;
    @FXML
    private TableColumn<GameInfoWithButton, String> foodColumn;
    @FXML
    private TableColumn<GameInfoWithButton, Button> connectButtonColumn;
    @FXML
    private TableColumn<PlayerWithScore, String> playerNameColumn;
    @FXML
    private TableColumn<PlayerWithScore, Integer> playerScoreColumn;
    @FXML
    private Label gameOwner;
    @FXML
    private Label foodAmount;
    @FXML
    private Label fieldSize;
    @FXML
    private TableView<PlayerWithScore> playersRankingTable;
    @FXML
    private Button exitButton;
    @FXML
    private Button newGameButton;
    @FXML
    private TableView<GameInfoWithButton> gameListTable;
    @FXML
    private BorderPane gameFieldPane;

    private final ObservableList<PlayerWithScore> playersObservableList = FXCollections.observableArrayList();
    private final ObservableList<GameInfoWithButton> gameInfoObservableList = FXCollections.observableArrayList();

    private Rectangle[][] fieldCells;


    private Stage stage;
    private GameConfig gameConfig;

    private GamePresenter gamePresenter;


    public void setGamePresenter(@NotNull GamePresenter presenter) {
        this.gamePresenter = Objects.requireNonNull(presenter, "Presenter cant be null");
    }

    public void setStage(@NotNull Stage stage) {
        this.stage = Objects.requireNonNull(stage, "Stage cant be null");
        this.stage.addEventHandler(KeyEvent.KEY_RELEASED, getMovementEventHandler());
        this.stage.setOnCloseRequest(event -> close());
        initPlayersInfoTable();
        initGameListTable();
        setActionOnButtons();
    }

    private void setActionOnButtons() {
        exitButton.setOnAction(event -> close());
        newGameButton.setOnAction(event -> gamePresenter.fireEvent(new NewGameEvent()));
    }

    private void close() {
        if (stage == null) {
            throw new IllegalStateException("Cant close not initialized stage");
        }
        stage.close();
        gamePresenter.fireEvent(new ExitEvent());
    }

    private EventHandler<KeyEvent> getMovementEventHandler() {
        return event -> {
            if (gamePresenter == null) {
                throw new IllegalStateException("Cant move with undefined presenter");
            }
            getDirectionByKeyCode(event.getCode())
                    .ifPresent(direction ->
                            gamePresenter.fireEvent(new MoveEvent(direction))
                    );
        };
    }

    private Optional<Direction> getDirectionByKeyCode(@NotNull KeyCode code) {
        return switch (code) {
            case UP, W -> Optional.of(Direction.UP);
            case DOWN, S -> Optional.of(Direction.DOWN);
            case RIGHT, D -> Optional.of(Direction.RIGHT);
            case LEFT, A -> Optional.of(Direction.LEFT);
            default -> Optional.empty();
        };
    }

    private void builtField() {
        if (gameConfig == null) {
            throw new IllegalStateException("Cant create field without config");
        }
        final int gameFieldHeight = gameConfig.getFieldHeight();
        final int gameFieldWidth = gameConfig.getFieldWidth();
        int rectHeight = (int) (gameFieldPane.getPrefHeight() / gameFieldHeight);
        int rectWidth = (int) (gameFieldPane.getPrefWidth() / gameFieldWidth);
        GridPane gridPane = new GridPane();
        fieldCells = new Rectangle[gameFieldHeight][gameFieldWidth];
        for (int row = 0; row < gameFieldHeight; ++row) {
            for (int col = 0; col < gameFieldWidth; ++col) {
                Rectangle rectangle = new Rectangle(rectWidth, rectHeight, EMPTY_CELL_COLOR);
                fieldCells[row][col] = rectangle;
                gridPane.add(rectangle, col, row);
            }
        }
        gridPane.setGridLinesVisible(true);
        gameFieldPane.setCenter(gridPane);
    }

    @Override
    public void drawFruit(@NotNull Point point) {
        paintPoint(point, FRUIT_COLOR);
    }

    @Override
    public void drawEmptyCell(@NotNull Point point) {
        paintPoint(point, EMPTY_CELL_COLOR);
    }

    @Override
    public void drawSnakePoint(@NotNull Point point, @NotNull Color playerSnakeColor) {
        paintPoint(point, playerSnakeColor);
    }

    private void paintPoint(@NotNull Point point, @NotNull Paint color) {
        try {
            fieldCells[point.getY()][point.getX()].setFill(color);
        } catch (Exception ignored) {} // :)
    }

    @Override
    public void updateCurrentGameInfo(@NotNull String owner, int gameFieldHeight, int gameFieldWidth, int foodNumber) {
        Platform.runLater(() -> {
            foodAmount.setText(String.valueOf(foodNumber));
            fieldSize.setText(gameFieldHeight + "x" + gameFieldWidth);
            gameOwner.setText(owner);
        });
    }

    @Override
    public void showUserListInfo(@NotNull List<PlayerWithScore> playerWithScoreList) {
        playersObservableList.setAll(playerWithScoreList);
    }

    @Override
    public void setConfig(@NotNull GameConfig gameConfig) {
        this.gameConfig = Objects.requireNonNull(gameConfig, "Config cant be null");
        builtField();
    }

    @Override
    public void showGameList(@NotNull Set<GameInfoWithButton> gameInfoWithButtons) {
        gameInfoWithButtons.forEach(gameInfoWithButton -> {
            Button button = gameInfoWithButton.getButton();
            button.setOnAction(event ->
                    gamePresenter.fireEvent(
                            new JoinToGameEvent(
                                    gameInfoWithButton.getMasterNode(),
                                    gameInfoWithButton.getMasterNodeName(),
                                    gameInfoWithButton.getConfig()
                            )
                    )
            );
        });
        gameInfoObservableList.setAll(gameInfoWithButtons);
    }


    private void initGameListTable() {
        gameListTable.setItems(gameInfoObservableList);
        masterColumn.setCellValueFactory(new PropertyValueFactory<>("masterNodeName"));
        foodColumn.setCellValueFactory(new PropertyValueFactory<>("foodNumber"));
        playersNumberColumn.setCellValueFactory(new PropertyValueFactory<>("playersNumber"));
        fieldSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fieldSize"));
        connectButtonColumn.setCellValueFactory(new PropertyValueFactory<>("button"));
    }

    private void initPlayersInfoTable() {
        playersRankingTable.setItems(playersObservableList);
        playerNameColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        playerScoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
    }
    @FXML
    void initialize() {
        assert connectButtonColumn != null : "fx:id=\"connectButtonColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert exitButton != null : "fx:id=\"exitButton\" was not injected: check your FXML file 'GameView.fxml'.";
        assert fieldSize != null : "fx:id=\"fieldSize\" was not injected: check your FXML file 'GameView.fxml'.";
        assert fieldSizeColumn != null : "fx:id=\"fieldSizeColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert foodAmount != null : "fx:id=\"foodAmount\" was not injected: check your FXML file 'GameView.fxml'.";
        assert foodColumn != null : "fx:id=\"foodColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert gameFieldPane != null : "fx:id=\"gameFieldPane\" was not injected: check your FXML file 'GameView.fxml'.";
        assert gameListTable != null : "fx:id=\"gameListTable\" was not injected: check your FXML file 'GameView.fxml'.";
        assert gameOwner != null : "fx:id=\"gameOwner\" was not injected: check your FXML file 'GameView.fxml'.";
        assert masterColumn != null : "fx:id=\"masterColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert newGameButton != null : "fx:id=\"newGameButton\" was not injected: check your FXML file 'GameView.fxml'.";
        assert playerNameColumn != null : "fx:id=\"playerNameColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert playerScoreColumn != null : "fx:id=\"playerScoreColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert playersNumberColumn != null : "fx:id=\"playersNumberColumn\" was not injected: check your FXML file 'GameView.fxml'.";
        assert playersRankingTable != null : "fx:id=\"playersRankingTable\" was not injected: check your FXML file 'GameView.fxml'.";

    }
}