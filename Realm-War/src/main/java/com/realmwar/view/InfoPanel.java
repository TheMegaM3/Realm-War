package com.realmwar.view;

import com.realmwar.engine.GameManager;
import com.realmwar.model.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class InfoPanel extends JPanel {
    private final JLabel playerLabel;
    private final JLabel goldValueLabel;
    private final JLabel foodValueLabel;
    private final JLabel unitValueLabel; // MODIFIED: Added label for units
    private final JLabel timerLabel;

    private final GameManager gameManager;

    private Image goldIcon;
    private Image foodIcon;
    private Image unitIcon; // MODIFIED: Added icon for units

    public InfoPanel(GameManager gameManager) {
        this.gameManager = gameManager;

        loadResourceIcons();

        setBackground(new Color(221, 213, 226));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JPanel currentPlayerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        currentPlayerInfoPanel.setOpaque(false);

        playerLabel = new JLabel("Player: Player1");
        timerLabel = new JLabel("Time: 30");

        Font boldFont = new Font("Arial", Font.BOLD, 16);
        Color textColor = new Color(54, 54, 54);

        playerLabel.setFont(boldFont);
        playerLabel.setForeground(textColor);
        timerLabel.setFont(boldFont);
        timerLabel.setForeground(textColor);

        Player currentPlayer = gameManager.getCurrentPlayer();
        goldValueLabel = new JLabel(String.valueOf(currentPlayer.getResourceHandler().getGold()));
        foodValueLabel = new JLabel(String.valueOf(currentPlayer.getResourceHandler().getFood()));
        // MODIFIED: Initialize the unit label
        unitValueLabel = new JLabel(currentPlayer.getCurrentUnitCount() + " / " + currentPlayer.getUnitCapacity(gameManager.getGameBoard()));


        JPanel goldPanel = createResourcePanel(goldIcon, goldValueLabel);
        JPanel foodPanel = createResourcePanel(foodIcon, foodValueLabel);
        // MODIFIED: Create the panel for the unit display
        JPanel unitPanel = createResourcePanel(unitIcon, unitValueLabel);

        currentPlayerInfoPanel.add(playerLabel);
        currentPlayerInfoPanel.add(goldPanel);
        currentPlayerInfoPanel.add(foodPanel);
        // MODIFIED: Add the unit panel to the UI
        currentPlayerInfoPanel.add(unitPanel);
        currentPlayerInfoPanel.add(timerLabel);

        add(currentPlayerInfoPanel, BorderLayout.WEST);

        JPanel colorKeyPanel = createPlayerColorKey();
        add(colorKeyPanel, BorderLayout.EAST);
    }

    private void loadResourceIcons() {
        try {
            goldIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/gold1.png")));
            foodIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/food.png")));
            // MODIFIED: Load the unit icon (ensure you have a 'unit_icon.png' in your assets folder)
            unitIcon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/unit_icon.png")));
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            System.err.println("Could not load resource icons. Make sure gold1.png, food.png, and unit_icon.png are present.");
            // e.printStackTrace();
        }
    }

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

    private Color getPlayerColor(String playerName) {
        if (playerName == null) return Color.GRAY;
        return switch (playerName) {
            case "Player 1" -> new Color(173, 216, 230);
            case "Player 2" -> new Color(255, 105, 97);
            case "Player 3" -> new Color(255, 209, 220);
            case "Player 4" -> new Color(204, 153, 204);
            default -> Color.GRAY;
        };
    }

    // MODIFIED: updateInfo now takes unit count/capacity and updates the new label.
    public void updateInfo(String playerName, int gold, int food, int unitCount, int unitCapacity) {
        playerLabel.setText("Player: " + playerName);
        goldValueLabel.setText(String.valueOf(gold));
        foodValueLabel.setText(String.valueOf(food));
        unitValueLabel.setText(unitCount + " / " + unitCapacity);
    }

    public void updateTimer(int seconds) {
        timerLabel.setText("Time: " + seconds);
    }
}
