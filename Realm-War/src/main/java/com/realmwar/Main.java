// Main.java
// Entry point for the RealmWar game.
// Initializes the database, prompts for the number of players, and starts the game UI.

package com.realmwar;

import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.util.Constants;
import com.realmwar.view.GameFrame;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

// Main class to start the game
public class Main {
    public static void main(String[] args) {
        // Initialize the database
        DatabaseManager.initializeDatabase();

        // Run UI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Prompt for number of players
            Object[] options = {2, 3, 4};
            Integer numPlayers = (Integer) JOptionPane.showInputDialog(
                    null,
                    "Select number of players:",
                    "Realm War Setup",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    2);

            // Exit if the user closes the dialog
            if (numPlayers == null) {
                System.exit(0);
                return;
            }

            // Create player names
            List<String> playerNames = new ArrayList<>();
            for (int i = 1; i <= numPlayers; i++) {
                playerNames.add("Player " + i);
            }

            // Initialize game manager
            GameManager gameManager = new GameManager(
                    playerNames,
                    Constants.DEFAULT_BOARD_WIDTH,
                    Constants.DEFAULT_BOARD_HEIGHT
            );

            // Create and display the game window
            GameFrame gameFrame = new GameFrame(gameManager);
            gameFrame.setVisible(true);
            gameFrame.updateView();
            gameFrame.resetAndStartTurnTimer();
        });
    }
}