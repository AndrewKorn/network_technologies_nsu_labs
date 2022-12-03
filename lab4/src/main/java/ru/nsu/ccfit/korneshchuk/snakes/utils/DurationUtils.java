package ru.nsu.ccfit.korneshchuk.snakes.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public final class DurationUtils {
    private DurationUtils() {
    }

    public static long betweenInMs(@NotNull Instant time1, @NotNull Instant time2) {
        return Duration.between(time1, time2).abs().toMillis();
    }
}
