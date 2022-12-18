package ru.nsu.ccfit.korneshchuk.snakes.config;


import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class Config implements GameConfig, Serializable {
    private final int fieldWidth;
    private final int fieldHeight;
    private final double deadSnakeToFoodProbability;
    private final int foodStaticNumber;
    private final int pingDelayMs;
    private final int stateDelayMs;
    private final int foodPerPlayer;
    private final int nodeTimeoutMs;
    private final String playerName;

    private Config(int fieldWidth,
                   int fieldHeight,
                   double deadSnakeToFoodProbability,
                   int foodStaticNumber,
                   int pingDelayMs,
                   int stateDelayMs,
                   int foodPerPlayer,
                   int nodeTimeoutMs,
                   @NotNull String playerName) {
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        this.deadSnakeToFoodProbability = deadSnakeToFoodProbability;
        this.foodStaticNumber = foodStaticNumber;
        this.pingDelayMs = pingDelayMs;
        this.stateDelayMs = stateDelayMs;
        this.foodPerPlayer = foodPerPlayer;
        this.nodeTimeoutMs = nodeTimeoutMs;
        this.playerName = playerName;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    public int getPingDelayMs() {
        return pingDelayMs;
    }

    public int getStateDelayMs() {
        return stateDelayMs;
    }

    public int getNodeTimeoutMs() {
        return nodeTimeoutMs;
    }

    @Override
    public int getFieldWidth() {
        return fieldWidth;
    }

    @Override
    public int getFieldHeight() {
        return fieldHeight;
    }

    @Override
    public int getFoodStaticNumber() {
        return foodStaticNumber;
    }

    @Override
    public int getFoodPerPlayer() {
        return foodPerPlayer;
    }


    @Override
    public double getProbabilityOfDeadSnakeCellsToFood() {
        return deadSnakeToFoodProbability;
    }

    public static final class Builder {
        private int fieldWidth = 40;
        private int fieldHeight = 30;
        private double deadSnakeToFoodProbability = 0.1;
        private int foodStaticNumber = 1;
        private int foodPerPlayer = 1;
        private int pingDelayMs = 100;
        private int stateDelayMs = 1000;
        private int nodeTimeoutMs = 800;
        private String playerName = "PLAYER";

        private Builder() {
        }

        public static Builder aConfig() {
            return new Builder();
        }

        public Builder withPlayerName(@NotNull String playerName) {
            this.playerName = Objects.requireNonNull(playerName);
            return this;
        }

        public Builder withFieldWidth(int fieldWidth) {
            this.fieldWidth = fieldWidth;
            return this;
        }

        public Builder withFieldHeight(int fieldHeight) {
            this.fieldHeight = fieldHeight;
            return this;
        }

        public Builder withDeadSnakeToFoodProbability(double deadSnakeToFoodProbability) {
            this.deadSnakeToFoodProbability = deadSnakeToFoodProbability;
            return this;
        }

        public Builder withFoodStaticNumber(int foodStaticNumber) {
            this.foodStaticNumber = foodStaticNumber;
            return this;
        }

        public Builder withPingDelayMs(int pingDelayMs) {
            this.pingDelayMs = pingDelayMs;
            return this;
        }

        public Builder withStateDelayMs(int stateDelayMs) {
            this.stateDelayMs = stateDelayMs;
            return this;
        }

        public Builder withFoodPerPlayer(int foodPerPlayer) {
            this.foodPerPlayer = foodPerPlayer;
            return this;
        }

        public Builder withNodeTimeoutMs(int nodeTimeoutMs) {
            this.nodeTimeoutMs = nodeTimeoutMs;
            return this;
        }

        public Config build() {
            Config config = new Config(
                    fieldWidth,
                    fieldHeight,
                    deadSnakeToFoodProbability,
                    foodStaticNumber,
                    pingDelayMs,
                    stateDelayMs,
                    foodPerPlayer,
                    nodeTimeoutMs,
                    playerName
            );
            ConfigValidator validator = new ConfigValidator(config);
            validator.validate();
            return config;
        }
    }
}
