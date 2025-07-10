package com.realmwar.view;

import com.realmwar.engine.GameBoard;
import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameBoardPanel extends JPanel {
    private GameBoard gameBoard;
    private GameEntity selectedEntity;
    private final GameManager gameManager;
    private final GameFrame gameFrame;
    private int selectedX = -1;
    private int selectedY = -1;
    private Unit attackingUnit;

    private static final Color PLAYER1_COLOR = new Color(173, 216, 230);   // Blue
    private static final Color PLAYER2_COLOR = new Color(255, 105, 97);   // Red
    private static final Color PLAYER3_COLOR = new Color(255, 209, 220);     // pink
    private static final Color PLAYER4_COLOR = new Color(204, 153, 204);   // purple
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);

    public GameBoardPanel(GameManager gameManager, GameFrame frame) {
        this.gameManager = gameManager;
        this.gameFrame = frame;
        setBackground(new Color(101, 67, 33));
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameBoard == null) return;
                int tileWidth = getWidth() / gameBoard.width;
                int tileHeight = getHeight() / gameBoard.height;
                selectedX = e.getX() / tileWidth;
                selectedY = e.getY() / tileHeight;
                gameManager.setSelectedTile(selectedX, selectedY);
                GameEntity clickedEntity = gameBoard.getTile(selectedX, selectedY).getEntity();

                if (gameFrame.isMergeModeActive()) {
                    if (clickedEntity instanceof Unit && clickedEntity.getOwner() == gameManager.getCurrentPlayer()) {
                        gameFrame.handleMergeClick((Unit) clickedEntity);
                    } else {
                        JOptionPane.showMessageDialog(GameBoardPanel.this, "Please select one of your units.", "Merge Error", JOptionPane.ERROR_MESSAGE);
                    }
                    repaint();
                    return;
                }

                if (attackingUnit != null) {
                    try {
                        gameManager.attackUnit(attackingUnit, clickedEntity);
                        attackingUnit = null; // Reset attack state after attempt
                        gameFrame.updateView(); // Update the whole view to reflect changes
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GameBoardPanel.this,
                                ex.getMessage(), "Attack error", JOptionPane.ERROR_MESSAGE);
                        attackingUnit = null; // Also reset on error
                    }
                }
                selectedEntity = clickedEntity; // Update selected entity after any action
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameBoard == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int tileWidth = getWidth() / gameBoard.width;
        int tileHeight = getHeight() / gameBoard.height;

        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                drawTile(g2d, x, y, tileWidth, tileHeight);
            }
        }

        if (selectedX >= 0 && selectedY >= 0) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(selectedX * tileWidth, selectedY * tileHeight, tileWidth, tileHeight);
        }

        if (attackingUnit != null) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(attackingUnit.getX() * tileWidth,
                    attackingUnit.getY() * tileHeight,
                    tileWidth, tileHeight);
        }
    }

    // Helper method to get a player's color based on their name.
    private Color getPlayerColor(String playerName) {
        if (playerName == null) return Color.GRAY;
        return switch (playerName) {
            case "Player 1" -> PLAYER1_COLOR;
            case "Player 2" -> PLAYER2_COLOR;
            case "Player 3" -> PLAYER3_COLOR;
            case "Player 4" -> PLAYER4_COLOR;
            default -> Color.GRAY; // A fallback color
        };
    }

    private void drawTile(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        Color baseColor = gameBoard.getTile(x, y).block.getColor();
        g2d.setColor(baseColor);
        g2d.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        g2d.setColor(baseColor.darker());
        g2d.drawRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        GameEntity entity = gameBoard.getTile(x, y).getEntity();
        if (entity != null) {
            if (entity instanceof Unit) {
                drawUnit(g2d, x, y, tileWidth, tileHeight, (Unit) entity);
            } else if (entity instanceof Structure) {
                drawStructure(g2d, x, y, tileWidth, tileHeight, (Structure) entity);
            }
        }
    }

    private void drawStructure(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Structure entity) {
        Color baseColor = getPlayerColor(entity.getOwner().getName());


        g2d.setColor(SHADOW_COLOR);
        g2d.fillRoundRect(x * tileWidth + 5, y * tileHeight + 5, tileWidth - 10, tileHeight - 10, 15, 15);

        g2d.setColor(baseColor);
        g2d.fillRoundRect(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 10, tileHeight - 10, 15, 15);

        String symbol = "";
        if (entity instanceof Farm) symbol = "🌾";
        else if (entity instanceof Barrack) symbol = "🛡️";
        else if (entity instanceof Market) symbol = "🏪";
        else if (entity instanceof Tower) symbol = "🏰";
        else if (entity instanceof TownHall) symbol = "🏛️";


        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x * tileWidth + (tileWidth - fm.stringWidth(symbol)) / 2;
        int textY = y * tileHeight + ((tileHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(symbol, textX, textY);

        //  Draw Health Bar
        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getDurability(), entity.getMaxDurability());
    }

    private void drawUnit(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Unit entity) {
       // Use the helper method to get the correct color.
        Color unitColor = getPlayerColor(entity.getOwner().getName());

        g2d.setColor(SHADOW_COLOR);
        g2d.fillOval(x * tileWidth + 5, y * tileHeight + 5, tileWidth - 10, tileHeight - 10);

        g2d.setColor(unitColor);
        g2d.fillOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 10, tileHeight - 10);

        String swordSymbol = "";
        if (entity instanceof Peasant) swordSymbol = "P";
        else if (entity instanceof Spearman) swordSymbol = "S";
        else if (entity instanceof Swordsman) swordSymbol = "W";
        else if (entity instanceof Knight) swordSymbol = "K";

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x * tileWidth + (tileWidth - fm.stringWidth(swordSymbol)) / 2;
        int textY = y * tileHeight + ((tileHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(swordSymbol, textX, textY);

        // Draw Health Bar
        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getHealth(), entity.getMaxHealth());
    }

    // method to draw a health bar for any entity.
    private void drawHealthBar(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, int currentHealth, int maxHealth) {
        if (maxHealth <= 0) return;

        double healthPercent = (double) currentHealth / maxHealth;

        int barWidth = tileWidth - 10;
        int barHeight = 8;
        int barX = x * tileWidth + 5;
        int barY = y * tileHeight + tileHeight - barHeight - 5; // Position near the bottom of the tile

        // Draw the background of the health bar (the "empty" part)
        g2d.setColor(new Color(60, 0, 0));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);

        // Determine health bar color
        Color healthColor;
        if (healthPercent > 0.6) {
            healthColor = new Color(34, 177, 76); // Green
        } else if (healthPercent > 0.3) {
            healthColor = new Color(255, 201, 14); // Yellow/Orange
        } else {
            healthColor = new Color(237, 28, 36); // Red
        }

        // Draw the current health portion
        g2d.setColor(healthColor);
        g2d.fillRoundRect(barX, barY, (int) (barWidth * healthPercent), barHeight, 5, 5);

        // border to the health bar for better visibility
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 5, 5);
    }


    public void updatePanel(GameBoard board, GameEntity selected) {
        this.gameBoard = board;
        this.selectedEntity = selected;
        // this.attackingUnit = null;
        if (this.selectedX == -1 && this.selectedY == -1) {
            this.selectedEntity = null;
        }
        repaint();
    }

    public void setAttackingUnit(Unit unit) {
        this.attackingUnit = unit;
        repaint();
    }
}
