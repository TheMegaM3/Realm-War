package com.realmwar.view;

import com.realmwar.engine.GameManager;
import com.realmwar.model.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class InfoPanel extends JPanel {
    private final JLabel playerLabel;
    private final JLabel goldValueLabel;
    private final JLabel foodValueLabel;
    private final JLabel timerLabel;


    private final GameManager gameManager;

    //Fields to hold the loaded resource icons
    private Image goldIcon;
    private Image foodIcon;

    public InfoPanel(GameManager gameManager) {
        this.gameManager = gameManager;

        loadResourceIcons();

        setBackground(new Color(221, 213, 226));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // --- Left side: Current Player Info ---
        JPanel currentPlayerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        currentPlayerInfoPanel.setOpaque(false); // Make it transparent

        playerLabel = new JLabel("Player: Player1");
        timerLabel = new JLabel("Time: 30");

        Font boldFont = new Font("Arial", Font.BOLD, 16);
        Color textColor = new Color(54, 54, 54);

        playerLabel.setFont(boldFont);
        playerLabel.setForeground(textColor);
        timerLabel.setFont(boldFont);
        timerLabel.setForeground(textColor);

        // MODIFIED: Initialize the value labels BEFORE creating the panels.
        // This ensures the class fields are the ones being used.
        goldValueLabel = new JLabel(String.valueOf(gameManager.getCurrentPlayer().getResourceHandler().getGold()));
        foodValueLabel = new JLabel(String.valueOf(gameManager.getCurrentPlayer().getResourceHandler().getFood()));

        // Create resource panels and pass the class-field labels to them
        JPanel goldPanel = createResourcePanel(goldIcon, goldValueLabel);
        JPanel foodPanel = createResourcePanel(foodIcon, foodValueLabel);

        currentPlayerInfoPanel.add(playerLabel);
        currentPlayerInfoPanel.add(goldPanel);
        currentPlayerInfoPanel.add(foodPanel);
        currentPlayerInfoPanel.add(timerLabel);

        add(currentPlayerInfoPanel, BorderLayout.WEST);

        // --- Right side: Player Color Key ---
        JPanel colorKeyPanel = createPlayerColorKey();
        add(colorKeyPanel, BorderLayout.EAST);
    }

    // Method to load resource icons
    private void loadResourceIcons() {
        try {
            goldIcon = ImageIO.read(getClass().getResource("/assets/gold1.png"));
            foodIcon = ImageIO.read(getClass().getResource("/assets/food.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load resource icons.");
            e.printStackTrace();
        }
    }

    // MODIFIED: This helper method now accepts the JLabel that needs to be displayed.
    private JPanel createResourcePanel(Image icon, JLabel valueLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        if (icon != null) {
            // Scale the icon to a nice size for the info bar
            Image scaledIcon = icon.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaledIcon));
        }

        // Set font and color for the value label that was passed in
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(new Color(54, 54, 54));

        panel.add(iconLabel);
        panel.add(valueLabel); // Add the actual class field label to the panel
        return panel;
    }

    private JPanel createPlayerColorKey() {
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        keyPanel.setOpaque(false);

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
        goldValueLabel.setText(String.valueOf(gold));
        foodValueLabel.setText(String.valueOf(food));
    }

    public void updateTimer(int seconds) {
        timerLabel.setText("Time: " + seconds);
    }

}