package main.java.com.realmwar.game.util;

public class GameLogger {
    public static void log(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void logWarning(String message) {
        System.out.println("[WARNING] " + message);
    }
    public static void logError(String message) {
        System.err.println("[ERROR] " + message);
    }
}

