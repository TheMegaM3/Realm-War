package com.realmwar;

import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.util.Constants;
import com.realmwar.view.GameFrame;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Initialize the database
        DatabaseManager.initializeDatabase();

        // Use invokeLater to ensure all Swing components are created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // --- Player Setup ---

            Object[] options = {2, 3, 4};
            Integer numPlayers = (Integer) JOptionPane.showInputDialog(
                    null, // parent component
                    "Select number of players:",
                    "Realm War Setup",
                    JOptionPane.PLAIN_MESSAGE,
                    null, // icon
                    options, // selection values
                    2); // default value

            // If the user closes the dialog, exit the application.
            if (numPlayers == null) {
                System.exit(0);
                return;
            }

            // Create the list of player names dynamically
            List<String> playerNames = new ArrayList<>();
            for (int i = 1; i <= numPlayers; i++) {
                playerNames.add("Player " + i);
            }

            // --- Game Initialization ---
            GameManager gameManager = new GameManager(
                    playerNames,
                    Constants.DEFAULT_BOARD_WIDTH,
                    Constants.DEFAULT_BOARD_HEIGHT
            );

            // Create and display the game window
            GameFrame gameFrame = new GameFrame(gameManager);
            gameFrame.setVisible(true); // Make the window visible.
            gameFrame.updateView();     // Perform an initial paint of the UI.
            gameFrame.resetAndStartTurnTimer(); // Start the first turn's timer.
        });
    }
}