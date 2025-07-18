// CustomExceptions.java
// Defines custom exceptions for the RealmWar game.
// Provides specific exception types for game rule violations to improve error handling.

package com.realmwar.util;

// Final class to hold custom exceptions, preventing instantiation
public final class CustomExceptions {
    // Private constructor to prevent instantiation
    private CustomExceptions() {}

    // Exception thrown when a game rule is violated (e.g., insufficient resources, invalid move)
    public static class GameRuleException extends Exception {
        // Constructor with a custom error message
        public GameRuleException(String message) {
            super(message);
        }
    }
}