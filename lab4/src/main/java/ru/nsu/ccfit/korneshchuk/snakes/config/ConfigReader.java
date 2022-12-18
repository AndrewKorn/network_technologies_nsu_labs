package ru.nsu.ccfit.korneshchuk.snakes.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class ConfigReader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);

    private ConfigReader() {
    }

    public static Config readConfig(@NotNull String configPath) {
        Objects.requireNonNull(configPath, "Config path cant be null");
        try (InputStream cfgStream = ConfigReader.class.getClassLoader().getResourceAsStream(configPath)) {
            Properties properties = new Properties();
            properties.load(cfgStream);
            return parseConfig(properties);
        } catch (IOException e) {
            logger.error("Cant open config file={}", configPath, e);
            throw new IllegalStateException("Cant open file ={" + configPath + "}", e);
        }
    }

    private static Config parseConfig(Properties properties) {
        Config.Builder builder = Config.Builder.aConfig();
        readDoubleProperty(properties, "food.deadProb")
                .ifPresent(builder::withDeadSnakeToFoodProbability);
        readIntegerProperty(properties, "food.perPlayer")
                .ifPresent(builder::withFoodPerPlayer);
        readIntegerProperty(properties, "field.height")
                .ifPresent(builder::withFieldHeight);
        readIntegerProperty(properties, "field.width")
                .ifPresent(builder::withFieldWidth);
        readIntegerProperty(properties, "food.static")
                .ifPresent(builder::withFoodStaticNumber);
        readIntegerProperty(properties, "node.timeout.ms")
                .ifPresent(builder::withNodeTimeoutMs);
        readIntegerProperty(properties, "ping.delay.ms")
                .ifPresent(builder::withPingDelayMs);
        readIntegerProperty(properties, "state.delay.ms")
                .ifPresent(builder::withStateDelayMs);
        Optional.ofNullable(properties.getProperty("player.name"))
                .ifPresent(builder::withPlayerName);
        return builder.build();
    }

    private static OptionalDouble readDoubleProperty(Properties properties, String key) {
        try {
            return OptionalDouble.of(
                    Double.parseDouble(properties.getProperty(key))
            );
        } catch (NumberFormatException e) {
            logger.error("Cant read double property by key = {}", key, e);
            return OptionalDouble.empty();
        }
    }

    private static OptionalInt readIntegerProperty(Properties properties, String key) {
        try {
            return OptionalInt.of(
                    Integer.parseInt(properties.getProperty(key), 10)
            );
        } catch (NumberFormatException e) {
            logger.error("Cant read integer property by key = {}", key, e);
            return OptionalInt.empty();
        }
    }
}
