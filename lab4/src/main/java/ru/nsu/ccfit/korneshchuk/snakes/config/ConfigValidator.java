package ru.nsu.ccfit.korneshchuk.snakes.config;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigValidator {
    private static final String ERROR_MESSAGE_FORMAT = "%s=%d not from valid interval=[%d, %d]";

    private static final int MIN_FIELD_WIDTH = 1;
    private static final int MAX_FIELD_WIDTH = 100;

    private static final int MIN_FIELD_HEIGHT = 1;
    private static final int MAX_FIELD_HEIGHT = 100;

    private static final int MIN_FOOD_PER_PLAYER = 0;
    private static final int MAX_FOOD_PER_PLAYER = 100;

    private static final int MIN_FOOD_STATIC = 0;
    private static final int MAX_FOOD_STATIC = 100;

    private static final int MIN_PING_DELAY_MS = 0;
    private static final int MAX_PING_DELAY_MS = 10000;

    private static final int MIN_STATE_DELAY_MS = 0;
    private static final int MAX_STATE_DELAY_MS = 10000;

    private static final int MIN_NODE_TIMEOUT_MS = 0;
    private static final int MAX_NODE_TIMEOUT_MS = 10000;

    private static final double MIN_DEAD_SNAKE_TO_FOOD_PROBABILITY = 0.0;
    private static final double MAX_DEAD_SNAKE_TO_FOOD_PROBABILITY = 1.0;


    @NotNull
    private final Config config;

    public ConfigValidator(@NotNull Config config) {
        this.config = Objects.requireNonNull(config);
    }

    public void validate() {
        validateField(config.getFieldWidth(), MIN_FIELD_WIDTH, MAX_FIELD_WIDTH, "field.width");
        validateField(config.getFieldHeight(), MIN_FIELD_HEIGHT, MAX_FIELD_HEIGHT, "field.height");
        validateField(config.getFoodPerPlayer(), MIN_FOOD_PER_PLAYER, MAX_FOOD_PER_PLAYER, "food.per.player");
        validateField(config.getFoodStaticNumber(), MIN_FOOD_STATIC, MAX_FOOD_STATIC, "food.static");
        validateField(config.getPingDelayMs(), MIN_PING_DELAY_MS, MAX_PING_DELAY_MS, "ping.delay");
        validateField(config.getStateDelayMs(), MIN_STATE_DELAY_MS, MAX_STATE_DELAY_MS, "state.delay");
        validateField(config.getNodeTimeoutMs(), MIN_NODE_TIMEOUT_MS, MAX_NODE_TIMEOUT_MS, "node.timeout");
        if (Double.compare(config.getProbabilityOfDeadSnakeCellsToFood(), MIN_DEAD_SNAKE_TO_FOOD_PROBABILITY) < 0
                || Double.compare(config.getProbabilityOfDeadSnakeCellsToFood(), MAX_DEAD_SNAKE_TO_FOOD_PROBABILITY) > 0) {
            throw new IllegalStateException(
                    String.format("%s=%f not from valid interval=[%f, %f]",
                            "deadSnake.to.food.probability",
                            config.getProbabilityOfDeadSnakeCellsToFood(),
                            MIN_DEAD_SNAKE_TO_FOOD_PROBABILITY,
                            MAX_DEAD_SNAKE_TO_FOOD_PROBABILITY
                    )
            );
        }
    }

    private void validateField(int fieldValue, int minValue, int maxValue, String fieldName) {
        if (fieldValue < minValue || fieldValue > maxValue) {
            throw new IllegalStateException(
                    String.format(ERROR_MESSAGE_FORMAT, fieldName, fieldValue, minValue, maxValue)
            );
        }
    }
}
