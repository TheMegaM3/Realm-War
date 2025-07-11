package com.realmwar.view;

import com.realmwar.engine.GameManager;
import com.realmwar.model.Player;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {
    private final JLabel playerLabel;
    private final JLabel goldLabel;
    private final JLabel foodLabel;
    private final JLabel timerLabel;


    private final GameManager gameManager;


    public InfoPanel(GameManager gameManager) {
        this.gameManager = gameManager;

        setBackground(new Color(221, 213, 226)); // Dusty lavender

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // --- Left side: Current Player Info ---
        JPanel currentPlayerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        currentPlayerInfoPanel.setOpaque(false); // Make it transparent

        playerLabel = new JLabel("Player: Player1");
        goldLabel = new JLabel("Gold: 500");
        foodLabel = new JLabel("Food: 200");
        timerLabel = new JLabel("Time: 30");

        Font boldFont = new Font("Arial", Font.BOLD, 16);
        playerLabel.setFont(boldFont);
        goldLabel.setFont(boldFont);
        foodLabel.setFont(boldFont);
        timerLabel.setFont(boldFont);


        Color textColor = new Color(54, 54, 54);
        playerLabel.setForeground(textColor);
        goldLabel.setForeground(textColor);
        foodLabel.setForeground(textColor);
        timerLabel.setForeground(textColor);

        currentPlayerInfoPanel.add(playerLabel);
        currentPlayerInfoPanel.add(goldLabel);
        currentPlayerInfoPanel.add(foodLabel);
        currentPlayerInfoPanel.add(timerLabel);

        add(currentPlayerInfoPanel, BorderLayout.WEST);

        // --- Right side: Player Color Key ---
        JPanel colorKeyPanel = createPlayerColorKey();
        add(colorKeyPanel, BorderLayout.EAST);
    }


    private JPanel createPlayerColorKey() {
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        keyPanel.setOpaque(false); // Make it transparent

        keyPanel.add(new JSeparator(SwingConstants.VERTICAL));

        for (Player player : gameManager.getPlayers()) {
            JPanel playerEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            playerEntryPanel.setOpaque(false);

            // Create a small colored square
            JPanel colorSwatch = new JPanel();
            colorSwatch.setBackground(getPlayerColor(player.getName()));
            colorSwatch.setPreferredSize(new Dimension(16, 16));
            colorSwatch.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

            // Create the player name label
            JLabel nameLabel = new JLabel(player.getName());
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            nameLabel.setForeground(new Color(54, 54, 54));

            playerEntryPanel.add(colorSwatch);
            playerEntryPanel.add(nameLabel);
            keyPanel.add(playerEntryPanel);
        }
        return keyPanel;
    }

    //  Helper method to get player colors, consistent with GameBoardPanel.
    private Color getPlayerColor(String playerName) {
        if (playerName == null) return Color.GRAY;
        return switch (playerName) {
            case "Player 1" -> new Color (173, 216, 230);
            case "Player 2" -> new Color (255, 105, 97);
            case "Player 3" -> new Color(255, 209, 220);
            case "Player 4" -> new Color(204, 153, 204);
            default -> Color.GRAY;
        };
    }

    public void updateInfo(String playerName, int gold, int food) {
        playerLabel.setText("Player: " + playerName);
        goldLabel.setText("Gold: " + gold);
        foodLabel.setText("Food: " + food);
    }

    public void updateTimer(int seconds) {
        timerLabel.setText("Time: " + seconds);
    }

}