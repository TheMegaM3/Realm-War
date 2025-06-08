package com.realmwar.view;

import com.realmwar.model.Player;
import javax.swing.*;
import java.awt.*;

/**
 * The top panel that displays information about the current player and game state.
 * This is a pure View component, updated by the GameController. It is designed to
 * work with the Player model that uses a separate ResourceHandler.
 */
public class InfoPanel extends JPanel {

    private final JLabel statusLabel;
    private final JLabel playerLabel;
    private final JLabel goldLabel;
    private final JLabel foodLabel;
    public final JButton nextTurnButton; // Public so the GameController can attach a listener

    public InfoPanel() {
        // Use a FlowLayout for a clean, single-row display
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        setBackground(new Color(70, 50, 30)); // Dark, thematic background
        setBorder(BorderFactory.createEtchedBorder());

        // Initialize all the labels and the button
        statusLabel = createInfoLabel("Status: Initializing...", Color.WHITE);
        playerLabel = createInfoLabel("Player: -", Color.CYAN);
        goldLabel = createInfoLabel("Gold: -", Color.YELLOW);
        foodLabel = createInfoLabel("Food: -", new Color(144, 238, 144));
        nextTurnButton = new JButton("End Turn");
        nextTurnButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Add components to the panel
        add(statusLabel);
        add(createSeparator());
        add(playerLabel);
        add(goldLabel);
        add(foodLabel);
        add(Box.createHorizontalStrut(30)); // Spacer
        add(nextTurnButton);
    }

    /**
     * This public method is called by the GameController to refresh the display
     * with the latest information from the model.
     * @param currentPlayer The current Player object from the GameManager.
     * @param gameStatus A string describing the current game state (e.g., "Game Over!").
     */
    public void updateInfo(Player currentPlayer, String gameStatus) {
        // Update the status label directly
        statusLabel.setText("Status: " + gameStatus);

        if (currentPlayer != null) {
            // Update player-specific info
            playerLabel.setText("Current Player: " + currentPlayer.getName());
            playerLabel.setForeground(currentPlayer.getName().equals("Player 1") ? Color.CYAN : new Color(255, 175, 175)); // Light Red/Pink for P2

            // Correctly get gold and food via the getResourceHandler() method
            goldLabel.setText("Gold: " + currentPlayer.getResourceHandler().getGold());
            foodLabel.setText("Food: " + currentPlayer.getResourceHandler().getFood());
        }

        // Disable the "End Turn" button if the game is over
        if (gameStatus.startsWith("GAME OVER")) {
            nextTurnButton.setEnabled(false);
        } else {
            nextTurnButton.setEnabled(true);
        }
    }

    /**
     * A private helper method to create styled JLabels consistently.
     */
    private JLabel createInfoLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        return label;
    }

    /**
     * A private helper to create a visual separator.
     */
    private Component createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 20));
        separator.setForeground(new Color(100, 80, 60));
        return separator;
    }
}
