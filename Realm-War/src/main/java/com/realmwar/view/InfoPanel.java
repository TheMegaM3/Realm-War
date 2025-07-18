// InfoPanel.java
// A Swing panel for displaying player information in the RealmWar game.
// Shows current player name, resources (gold and food), and turn timer with icons.

package com.realmwar.view;

import com.realmwar.engine.GameManager;
import com.realmwar.model.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

// Swing panel for displaying player information
public class InfoPanel extends JPanel {
    // Labels for displaying player info and timer
    private final JLabel playerLabel;
    private final JLabel goldValueLabel;
    private final JLabel foodValueLabel;
    private final JLabel timerLabel;
    // Reference to the game manager
    private final GameManager gameManager;
    // Icons for resources
    private Image goldIcon;
    private Image foodIcon;

    // Constructor to initialize the info panel
    public InfoPanel(GameManager gameManager) {
        this.gameManager = gameManager;

        // Load resource icons
        loadResourceIcons();

        // Panel setup
        setBackground(new Color(221, 213, 226)); // Light purple background
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Left side: Current player info
        JPanel currentPlayerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        currentPlayerInfoPanel.setOpaque(false); // Transparent background

        // Initialize labels
        playerLabel = new JLabel("Player: Player1");
        timerLabel = new JLabel("Time: 30");
        goldValueLabel = new JLabel(String.valueOf(gameManager.getCurrentPlayer().getResourceHandler().getGold()));
        foodValueLabel = new JLabel(String.valueOf(gameManager.getCurrentPlayer().getResourceHandler().getFood()));

        // Set font and color for labels
        Font boldFont = new Font("Arial", Font.BOLD, 16);
        Color textColor = new Color(54, 54, 54);
        playerLabel.setFont(boldFont);
        playerLabel.setForeground(textColor);
        timerLabel.setFont(boldFont);
        timerLabel.setForeground(textColor);

        // Create resource panels
        JPanel goldPanel = createResourcePanel(goldIcon, goldValueLabel);
        JPanel foodPanel = createResourcePanel(foodIcon, foodValueLabel);

        // Add components to the player info panel
        currentPlayerInfoPanel.add(playerLabel);
        currentPlayerInfoPanel.add(goldPanel);
        currentPlayerInfoPanel.add(foodPanel);
        currentPlayerInfoPanel.add(timerLabel);

        add(currentPlayerInfoPanel, BorderLayout.WEST);

        // Right side: Player color key
        JPanel colorKeyPanel = createPlayerColorKey();
        add(colorKeyPanel, BorderLayout.EAST);
    }

    // Loads resource icons
    private void loadResourceIcons() {
        try {
            goldIcon = ImageIO.read(getClass().getResource("/assets/gold1.png"));
            foodIcon = ImageIO.read(getClass().getResource("/assets/food.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load resource icons.");
            e.printStackTrace();
        }
    }

    // Creates a panel for displaying a resource with its icon and value
    private JPanel createResourcePanel(Image icon, JLabel valueLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        if (icon != null) {
            Image scaledIcon = icon.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaledIcon));
        }

        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(new Color(54, 54, 54));

        panel.add(iconLabel);
        panel.add(valueLabel);
        return panel;
    }

    // Creates a panel showing player color keys
    private JPanel createPlayerColorKey() {
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        keyPanel.setOpaque(false);

        keyPanel.add(new JSeparator(SwingConstants.VERTICAL));

        for (Player player : gameManager.getPlayers()) {
            JPanel playerEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            playerEntryPanel.setOpaque(false);

            JPanel colorSwatch = new JPanel();
            colorSwatch.setBackground(getPlayerColor(player.getName()));
            colorSwatch.setPreferredSize(new Dimension(16, 16));
            colorSwatch.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

            JLabel nameLabel = new JLabel(player.getName());
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            nameLabel.setForeground(new Color(54, 54, 54));

            playerEntryPanel.add(colorSwatch);
            playerEntryPanel.add(nameLabel);
            keyPanel.add(playerEntryPanel);
        }
        return keyPanel;
    }

    // Gets the color for a player based on their name
    private Color getPlayerColor(String playerName) {
        if (playerName == null) return Color.GRAY;
        return switch (playerName) {
            case "Player 1" -> new Color(173, 216, 230); // Light blue
            case "Player 2" -> new Color(255, 105, 97); // Coral
            case "Player 3" -> new Color(255, 209, 220); // Light pink
            case "Player 4" -> new Color(204, 153, 204); // Light purple
            default -> Color.GRAY;
        };
    }

    // Updates player info display
    public void updateInfo(String playerName, int gold, int food) {
        playerLabel.setText("Player: " + playerName);
        goldValueLabel.setText(String.valueOf(gold));
        foodValueLabel.setText(String.valueOf(food));
    }

    // Updates the timer display
    public void updateTimer(int seconds) {
        timerLabel.setText("Time: " + seconds);
    }
}