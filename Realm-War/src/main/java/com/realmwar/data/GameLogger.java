package com.realmwar.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A utility class for logging game events to a text file AND the system console.
 * All methods are static for easy access from anywhere in the application.
 */
public final class GameLogger {

    // The name of the log file that will be created.
    private static final String LOG_FILE = "gamelog.txt";
    // Defines the format for the timestamp, e.g., "2025/07/12 02:30:00".
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // A private constructor prevents this utility class from being instantiated.
    private GameLogger() {}

    /**
     * Logs a message to both the log file and the system console, prefixed with a timestamp.
     * @param message The message to log.
     */
    public static void log(String message) {
        // Create the full log message once to ensure consistency.
        String formattedMessage = dtf.format(LocalDateTime.now()) + " - " + message;

        // Print the log message to the system console (e.g., the terminal in the IDE).
        System.out.println("LOG: " + formattedMessage);

        // Use 'try-with-resources' to automatically close the writers, preventing resource leaks.
        // The 'true' argument in FileWriter enables append mode, so new logs are added to the end of the file.
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            // Write the formatted message to the file.
            pw.println(formattedMessage);

        } catch (IOException e) {
            // If the file cannot be written to, print an error to the standard error stream.
            System.err.println("CRITICAL: Could not write to log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
