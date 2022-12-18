package ru.nsu.ccfit.korneshchuk.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.SelectionKey;
import java.util.Objects;

@UtilityClass
public class SelectionKeyUtils {
    public static void turnOnWriteOption(@NotNull SelectionKey key) {
        Objects.requireNonNull(key, "Key cant be null");
        key.interestOpsOr(SelectionKey.OP_WRITE);
    }

    public static void turnOffWriteOption(@NotNull SelectionKey key) {
        Objects.requireNonNull(key, "Key cant be null");
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
    }

    public static void turnOnReadOption(@NotNull SelectionKey key) {
        Objects.requireNonNull(key, "Key cant be null");
        key.interestOpsOr(SelectionKey.OP_READ);
    }

    public static void turnOffReadOption(@NotNull SelectionKey key) {
        Objects.requireNonNull(key, "Key cant be null");
        key.interestOpsAnd(~SelectionKey.OP_READ);
    }
}