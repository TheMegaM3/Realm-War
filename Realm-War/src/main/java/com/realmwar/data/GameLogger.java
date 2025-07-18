// GameLogger.java
// A utility class for logging game events to both a file and the console, with timestamped messages.
// Designed as a singleton with static methods for easy access across the application.

package com.realmwar.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Utility class for logging game events
public final class GameLogger {

    // Name of the log file where messages are saved
    private static final String LOG_FILE = "gamelog.txt";
    // Formatter for creating timestamp strings in the format "yyyy/MM/dd HH:mm:ss"
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // Private constructor to prevent instantiation
    private GameLogger() {}

    // Logs a message to both the console and the log file with a timestamp
    public static void log(String message) {
        // Format the message with the current timestamp
        String formattedMessage = dtf.format(LocalDateTime.now()) + " - " + message;

        // Output to console
        System.out.println("LOG: " + formattedMessage);

        // Write to log file using try-with-resources for safe resource management
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            // Append the formatted message to the log file
            pw.println(formattedMessage);

        } catch (IOException e) {
            // Log error to console if file writing fails
            System.err.println("CRITICAL: Could not write to log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}