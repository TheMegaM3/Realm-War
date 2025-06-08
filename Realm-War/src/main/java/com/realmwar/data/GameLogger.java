package com.realmwar.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A utility class for logging game events to a text file.
 * All methods are static for easy access from anywhere in the application.
 */
public final class GameLogger {

    private static final String LOG_FILE = "gamelog.txt";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // Private constructor to prevent instantiation of this utility class.
    private GameLogger() {}

    /**
     * Logs a message to the game's log file with a timestamp.
     * @param message The message to write to the log.
     */
    public static void log(String message) {
        // Using try-with-resources ensures the writer is always closed.
        // The 'true' argument enables append mode.
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(dtf.format(LocalDateTime.now()) + " - " + message);

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not write to log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
