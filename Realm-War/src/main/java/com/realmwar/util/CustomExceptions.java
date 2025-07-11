package com.realmwar.util;

/**
 * A container for all custom, checked exceptions used in the game engine.
 * Using a custom exception allows for more specific error handling (e.g., in a try-catch block)
 * than using a generic Exception.
 */
public final class CustomExceptions {
    private CustomExceptions() {}

    /**
     * Thrown when a player action violates a rule of the game
     * (e.g., not enough resources, target out of range).
     */
    public static class GameRuleException extends Exception {
        public GameRuleException(String message) {
            super(message);
        }
    }
}
