package ru.nsu.ccfit.korneshchuk.snakes.gui.utils;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.net.NetNode;
import ru.nsu.ccfit.korneshchuk.snakes.config.Config;
import ru.nsu.ccfit.korneshchuk.snakes.config.GameConfig;
import ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker.GameInfo;

import java.util.Objects;

public class GameInfoWithButton {
    private final @NotNull NetNode masterNode;
    private final int playersNumber;
    @NotNull
    private final GameConfig config;
    private final boolean canJoin;

    private final String masterNodeName;
    private final String fieldSize;
    private final String foodNumber;

    private final Button button;

    public GameInfoWithButton(@NotNull GameInfo gameInfo) {
        Objects.requireNonNull(gameInfo);
        this.playersNumber = gameInfo.getPlayersNumber();
        this.config = gameInfo.getConfig();
        this.masterNode = gameInfo.getMasterNode();
        this.canJoin = gameInfo.isCanJoin();
        this.button = new Button("Вход");
        this.fieldSize = config.getFieldHeight() + "x" + config.getFieldWidth();
        this.foodNumber = config.getFoodStaticNumber() + ": x" + config.getFoodPerPlayer();
        this.masterNodeName = ((Config) config).getPlayerName();
        designButton();
    }

    private void designButton() {
        button.setBackground(new Background(new BackgroundFill(Color.BLUE, new CornerRadii(0), new Insets(0))));
        button.setTextFill(Color.WHITE);
    }

    @NotNull
    public GameConfig getConfig() {
        return config;
    }

    @NotNull
    public NetNode getMasterNode() {
        return masterNode;
    }

    @NotNull
    public Button getButton() {
        return button;
    }

    @NotNull
    public String getFoodNumber() {
        return foodNumber;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    @NotNull
    public String getMasterNodeName() {
        return masterNodeName;
    }

    @NotNull
    public String getFieldSize() {
        return fieldSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameInfoWithButton that = (GameInfoWithButton) o;
        return masterNode.equals(that.masterNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterNode);
    }
}
