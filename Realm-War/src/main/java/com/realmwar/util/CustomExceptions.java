package com.realmwar.util;

/**
 * A container for all custom, checked exceptions used in the game engine.
 * This allows for specific error handling by the controller.
 */
public final class CustomExceptions {
    private CustomExceptions() {}

    public static class GameRuleException extends Exception {
        public GameRuleException(String message) {
            super(message);
        }
    }
}
