package ru.nsu.ccfit.korneshchuk.utils;

import lombok.experimental.UtilityClass;

import java.util.function.IntPredicate;

@UtilityClass
public final class PortValidator {
    private static final IntPredicate portValidationPredicate = port -> port > 0;

    public static void validate(int port) {
        if (!portValidationPredicate.test(port)) {
            throw new IllegalArgumentException("Port is wrong, expected positive port, actual=" + port);
        }
    }

    public static boolean isValid(int port) {
        return portValidationPredicate.test(port);
    }
}