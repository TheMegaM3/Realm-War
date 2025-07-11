package com.realmwar.view;

import com.realmwar.engine.GameBoard;
import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;

import javax.imageio.ImageIO; // Import for handling images
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException; // Import for handling potential errors

public class GameBoardPanel extends JPanel {
    private GameBoard gameBoard;
    private GameEntity selectedEntity;
    private final GameManager gameManager;
    private final GameFrame gameFrame;
    private int selectedX = -1;
    private int selectedY = -1;
    private Unit attackingUnit;

    private Image peasantImage;
    private Image spearmanImage;
    private Image swordsmanImage;
    private Image knightImage;
    private Image farmImage;
    private Image barrackImage;
    private Image marketImage;
    private Image townhallImage;
    private Image towerImage;
    private static final Color PLAYER1_COLOR = new Color(173, 216, 230);
    private static final Color PLAYER2_COLOR = new Color(255, 105, 97);
    private static final Color PLAYER3_COLOR = new Color(255, 209, 220);
    private static final Color PLAYER4_COLOR = new Color(204, 153, 204);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 50);

    public GameBoardPanel(GameManager gameManager, GameFrame frame) {
        this.gameManager = gameManager;
        this.gameFrame = frame;

        setBackground(new Color(250, 240, 230));
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));

        loadAssetImages(); // MODIFIED: Call the renamed method

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

    // MODIFIED: Method renamed and updated to load ALL asset images.
    private void loadAssetImages() {
        try {
            // Load Unit Images
            peasantImage = ImageIO.read(getClass().getResource("/assets/peasant.png"));
            spearmanImage = ImageIO.read(getClass().getResource("/assets/spearman.png"));
            swordsmanImage = ImageIO.read(getClass().getResource("/assets/swordsman.png"));
            knightImage = ImageIO.read(getClass().getResource("/assets/knight.png"));

            // NEW: Load Structure Images
            townhallImage = ImageIO.read(getClass().getResource("/assets/townhall.png"));
            farmImage = ImageIO.read(getClass().getResource("/assets/farm.png"));
            barrackImage = ImageIO.read(getClass().getResource("/assets/barrack.png"));
            marketImage = ImageIO.read(getClass().getResource("/assets/market.png"));
            townhallImage = ImageIO.read(getClass().getResource("/assets/townhall1.png"));
            towerImage = ImageIO.read(getClass().getResource("/assets/tower.png"));

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading asset images. Make sure all images are in the src/assets folder.");
            e.printStackTrace();
        }
    }

    // ... (paintComponent, getPlayerColor, drawTile, and drawStructure methods remain the same) ...
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
            // MODIFIED: Using a softer golden yellow for selection
            g2d.setColor(new Color(255, 223, 100));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(selectedX * tileWidth, selectedY * tileHeight, tileWidth, tileHeight);
        }

        if (attackingUnit != null) {
            // MODIFIED: Using your Pastel Red for the attack highlight
            g2d.setColor(new Color(255, 105, 97));
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

        // Add a subtle border to each tile
        g2d.setColor(baseColor.darker().darker());
        g2d.drawRect(x * tileWidth, y * tileHeight, tileWidth -1, tileHeight -1);

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
        g2d.fillRoundRect(x * tileWidth + 7, y * tileHeight + 7, tileWidth - 10, tileHeight - 10, 15, 15);

        g2d.setColor(baseColor);
        g2d.fillRoundRect(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6, 12, 12);
        g2d.setColor(baseColor.darker());
        g2d.drawRoundRect(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6, 12, 12);

        // Select the correct image for the structure
        Image structureImage = null;
        if (entity instanceof TownHall) structureImage = townhallImage;
        else if (entity instanceof Farm) structureImage = farmImage;
        else if (entity instanceof Barrack) structureImage = barrackImage;
        else if (entity instanceof Market) structureImage = marketImage;
        else if (entity instanceof Tower) structureImage = towerImage;

        // Draw the image on top of the colored base
        if (structureImage != null) {
            int padding = (int) (tileWidth * 0.1); // Padding to make the image fit nicely
            g2d.drawImage(structureImage,
                          x * tileWidth + padding,
                          y * tileHeight + padding,
                          tileWidth - (padding * 2),
                          tileHeight - (padding * 2),
                          null);
        }


        //  Draw Health Bar
        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getDurability(), entity.getMaxDurability());
    }

    private void drawUnit(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Unit entity) {
        // First, draw the colored oval for the player's color
        Color unitColor = getPlayerColor(entity.getOwner().getName());

        g2d.setColor(SHADOW_COLOR);
        g2d.fillOval(x * tileWidth + 7, y * tileHeight + 7, tileWidth - 10, tileHeight - 10);

        // Draw main body with a white border
        g2d.setColor(unitColor);
        g2d.fillOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6);
        g2d.setColor(unitColor.darker());
        g2d.drawOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6);

        // select the correct image to draw
        Image unitImage = null;
        if (entity instanceof Peasant) unitImage = peasantImage;
        else if (entity instanceof Spearman) unitImage = spearmanImage;
        else if (entity instanceof Swordsman) unitImage = swordsmanImage;
        else if (entity instanceof Knight) unitImage = knightImage;

        // Draw the image on top of the colored oval
        if (unitImage != null) {
            // The padding values center the image inside the tile
            int padding = (int) (tileWidth * 0.15);
            g2d.drawImage(unitImage,
                          x * tileWidth + padding,
                          y * tileHeight + padding,
                          tileWidth - (padding * 2),
                          tileHeight - (padding * 2),
                          null);
        }

        // Finally, draw the health bar
        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getHealth(), entity.getMaxHealth());
    }

    // method to draw a health bar for any entity.
    private void drawHealthBar(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, int currentHealth, int maxHealth) {
        if (maxHealth <= 0) return;

        double healthPercent = (double) currentHealth / maxHealth;

        int barWidth = tileWidth - 14;
        int barHeight = 8;
        int barX = x * tileWidth + 7;
        int barY = y * tileHeight + tileHeight - barHeight - 5;

        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);

        Color healthColor = (healthPercent > 0.6) ? new Color(34, 177, 76) :
                            (healthPercent > 0.3) ? new Color(255, 201, 14) :
                                                    new Color(237, 28, 36);

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
