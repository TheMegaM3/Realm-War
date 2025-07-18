// GameBoardPanel.java
// A Swing panel for rendering the game board in the RealmWar game.
// Handles drawing tiles, units, structures, and user interactions like clicking for movement and attacks.

package com.realmwar.view;

import com.realmwar.engine.GameBoard;
import com.realmwar.engine.GameManager;
import com.realmwar.engine.GameTile;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Swing panel for rendering and interacting with the game board
public class GameBoardPanel extends JPanel {
    // Reference to the game board
    private GameBoard gameBoard;
    // Currently selected entity
    private GameEntity selectedEntity;
    // Reference to the game manager
    private final GameManager gameManager;
    // Reference to the game frame for UI updates
    private final GameFrame gameFrame;
    // Coordinates of the currently selected tile
    private int selectedX = -1;
    private int selectedY = -1;

    // Images for rendering units and structures
    private Image peasantImage;
    private Image spearmanImage;
    private Image swordsmanImage;
    private Image knightImage;
    private Image farmImage;
    private Image barrackImage;
    private Image marketImage;
    private Image townhallImage;
    private Image towerImage;

    // Player colors for visualization
    private static final Color PLAYER1_COLOR = new Color(173, 216, 230); // Light blue
    private static final Color PLAYER2_COLOR = new Color(255, 105, 97); // Coral
    private static final Color PLAYER3_COLOR = new Color(255, 209, 220); // Light pink
    private static final Color PLAYER4_COLOR = new Color(204, 153, 204); // Light purple
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 50); // Semi-transparent shadow
    // List of tiles where the selected unit can move
    private List<Point> movableTiles = new ArrayList<>();

    // Constructor to initialize the panel with game manager and frame
    public GameBoardPanel(GameManager gameManager, GameFrame gameFrame) {
        this.gameManager = gameManager;
        this.gameFrame = gameFrame;

        // Set panel appearance
        setBackground(new Color(250, 240, 230)); // Light cream background
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));

        // Load asset images for units and structures
        loadAssetImages();

        // Add mouse listener for handling tile clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameBoard == null) return;

                // Calculate drawable area considering insets
                Insets insets = getInsets();
                int drawableWidth = getWidth() - insets.left - insets.right;
                int drawableHeight = getHeight() - insets.top - insets.bottom;

                if (drawableWidth <= 0 || drawableHeight <= 0) return;

                // Calculate tile dimensions
                int tileWidth = drawableWidth / gameBoard.width;
                int tileHeight = drawableHeight / gameBoard.height;

                // Adjust mouse coordinates for insets
                int mouseX = e.getX() - insets.left;
                int mouseY = e.getY() - insets.top;

                // Determine clicked tile
                if (mouseX >= 0 && mouseX < drawableWidth && mouseY >= 0 && mouseY < drawableHeight) {
                    selectedX = mouseX / tileWidth;
                    selectedY = mouseY / tileHeight;

                    gameManager.setSelectedTile(selectedX, selectedY);
                    GameTile clickedTile = gameBoard.getTile(selectedX, selectedY);
                    if (clickedTile != null) {
                        GameEntity clickedEntity = clickedTile.getEntity();
                        handleGameActions(clickedEntity);
                    }
                } else {
                    selectedX = -1;
                    selectedY = -1;
                }
                repaint();
            }
        });
    }

    // Handles game actions based on clicked entity (selection, movement, attack, or merge)
    private void handleGameActions(GameEntity clickedEntity) {
        GameTile clickedTile = gameBoard.getTile(selectedX, selectedY);
        if (clickedTile == null) return;

        // Handle merge mode
        if (gameFrame.isMergeModeActive()) {
            if (clickedEntity instanceof Unit && clickedEntity.getOwner() == gameManager.getCurrentPlayer()) {
                gameFrame.handleMergeClick((Unit) clickedEntity);
            } else {
                JOptionPane.showMessageDialog(this, "Please select one of your units.", "Merge Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // Handle unit selection and actions
        Unit currentSelectedUnit = gameManager.getSelectedUnit();
        if (currentSelectedUnit == null) {
            // Select a unit if it belongs to the current player and hasn't acted
            if (clickedEntity instanceof Unit unit
                    && unit.getOwner() == gameManager.getCurrentPlayer()
                    && !unit.hasActedThisTurn()) {
                gameManager.setSelectedUnit(unit);
                highlightMovableTiles(unit);
            } else {
                gameManager.setSelectedUnit(null);
                clearHighlights();
                if (clickedEntity != null && !(clickedEntity instanceof Unit && clickedEntity.getOwner() == gameManager.getCurrentPlayer())) {
                    JOptionPane.showMessageDialog(this, "Please select one of your units that hasn't acted.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            // Handle movement or attack
            if (!currentSelectedUnit.hasActedThisTurn() && currentSelectedUnit.moveTo(clickedTile, gameBoard)) {
                currentSelectedUnit.setHasActedThisTurn(true);
                gameManager.setSelectedUnit(null);
                clearHighlights();
                gameFrame.updateView();
            } else if (clickedEntity != null && clickedEntity.getOwner() != gameManager.getCurrentPlayer()) {
                try {
                    gameManager.attackUnit(currentSelectedUnit, clickedEntity);
                    gameManager.setSelectedUnit(null);
                    clearHighlights();
                    gameFrame.updateView();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Attack Error", JOptionPane.ERROR_MESSAGE);
                    gameManager.setSelectedUnit(null);
                    clearHighlights();
                    gameFrame.updateView();
                }
            } else {
                gameManager.setSelectedUnit(null);
                clearHighlights();
                if (clickedEntity != null && clickedEntity.getOwner() == gameManager.getCurrentPlayer()) {
                    JOptionPane.showMessageDialog(this, "Cannot attack your own units.", "Attack Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        selectedEntity = clickedEntity;
    }

    // Highlights tiles where the selected unit can move
    private void highlightMovableTiles(Unit unit) {
        movableTiles.clear();
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                GameTile tile = gameBoard.getTile(x, y);
                if (unit.canMoveTo(tile, gameBoard)) {
                    movableTiles.add(new Point(x, y));
                }
            }
        }
        repaint();
    }

    // Clears highlighted tiles
    private void clearHighlights() {
        movableTiles.clear();
        repaint();
    }

    // Loads images for units and structures from assets
    private void loadAssetImages() {
        try {
            peasantImage = ImageIO.read(getClass().getResource("/assets/peasant2.png"));
            spearmanImage = ImageIO.read(getClass().getResource("/assets/spearman1.png"));
            swordsmanImage = ImageIO.read(getClass().getResource("/assets/swordsman2.png"));
            knightImage = ImageIO.read(getClass().getResource("/assets/knight2.png"));
            townhallImage = ImageIO.read(getClass().getResource("/assets/townhall2.png"));
            farmImage = ImageIO.read(getClass().getResource("/assets/farm2.png"));
            barrackImage = ImageIO.read(getClass().getResource("/assets/barrack2.png"));
            marketImage = ImageIO.read(getClass().getResource("/assets/market2.png"));
            towerImage = ImageIO.read(getClass().getResource("/assets/tower2.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading asset images. Make sure all images are in the src/assets folder.");
            e.printStackTrace();
        }
    }

    // Paints the panel, rendering tiles, units, structures, and highlights
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameBoard == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate drawable area
        Insets insets = getInsets();
        int drawableWidth = getWidth() - insets.left - insets.right;
        int drawableHeight = getHeight() - insets.top - insets.bottom;

        if (drawableWidth <= 0 || drawableHeight <= 0) return;

        // Calculate tile dimensions
        int tileWidth = drawableWidth / gameBoard.width;
        int tileHeight = drawableHeight / gameBoard.height;

        g.translate(insets.left, insets.top);

        // Draw tiles with territory colors
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                drawTile(g2d, x, y, tileWidth, tileHeight);
            }
        }

        // Draw selected tile highlight
        if (selectedX >= 0 && selectedY >= 0) {
            g2d.setColor(new Color(255, 223, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(selectedX * tileWidth, selectedY * tileHeight, tileWidth, tileHeight);
        }

        // Draw selected unit highlight
        Unit selectedUnit = gameManager.getSelectedUnit();
        if (selectedUnit != null) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(selectedUnit.getX() * tileWidth, selectedUnit.getY() * tileHeight, tileWidth, tileHeight);
        }

        // Draw movable tiles highlight
        g2d.setColor(new Color(144, 238, 144, 128));
        for (Point p : movableTiles) {
            int drawX = p.x * tileWidth;
            int drawY = p.y * tileHeight;
            g2d.fillRect(drawX, drawY, tileWidth, tileHeight);
        }

        g.translate(-insets.left, -insets.top);
    }

    // Gets the color for a player based on their name
    private Color getPlayerColor(String playerName) {
        if (playerName == null) return Color.GRAY;
        return switch (playerName) {
            case "Player 1" -> PLAYER1_COLOR;
            case "Player 2" -> PLAYER2_COLOR;
            case "Player 3" -> PLAYER3_COLOR;
            case "Player 4" -> PLAYER4_COLOR;
            default -> Color.GRAY;
        };
    }

    // Draws a single tile, including terrain, territory, and entity
    private void drawTile(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        GameTile tile = gameBoard.getTile(x, y);
        Color baseColor = tile.block.getColor();
        Player territoryOwner = tile.getTerritoryOwner();

        // Draw territory or base terrain color
        if (territoryOwner != null) {
            Color territoryColor = getPlayerColor(territoryOwner.getName());
            g2d.setColor(new Color(territoryColor.getRed(), territoryColor.getGreen(), territoryColor.getBlue(), 128));
            g2d.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
        } else {
            g2d.setColor(baseColor);
            g2d.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
        }

        // Draw tile border
        g2d.setColor(baseColor.darker().darker());
        g2d.drawRect(x * tileWidth, y * tileHeight, tileWidth - 1, tileHeight - 1);

        // Draw entity (unit or structure)
        GameEntity entity = tile.getEntity();
        if (entity != null) {
            if (entity instanceof Unit unit) {
                drawUnit(g2d, x, y, tileWidth, tileHeight, unit);
            } else if (entity instanceof Structure structure) {
                drawStructure(g2d, x, y, tileWidth, tileHeight, structure);
            }
        }
    }

    // Draws a structure with its image and health bar
    private void drawStructure(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Structure entity) {
        Color baseColor = getPlayerColor(entity.getOwner().getName());
        // Draw shadow
        g2d.setColor(SHADOW_COLOR);
        g2d.fillRoundRect(x * tileWidth + 7, y * tileHeight + 7, tileWidth - 10, tileHeight - 10, 15, 15);
        // Draw structure
        g2d.setColor(baseColor);
        g2d.fillRoundRect(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6, 12, 12);
        g2d.setColor(baseColor.darker());
        g2d.drawRoundRect(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6, 12, 12);

        // Draw structure image
        Image structureImage = null;
        if (entity instanceof TownHall) structureImage = townhallImage;
        else if (entity instanceof Farm) structureImage = farmImage;
        else if (entity instanceof Barrack) structureImage = barrackImage;
        else if (entity instanceof Market) structureImage = marketImage;
        else if (entity instanceof Tower) structureImage = towerImage;

        if (structureImage != null) {
            int padding = (int) (tileWidth * 0.1);
            g2d.drawImage(structureImage,
                    x * tileWidth + padding,
                    y * tileHeight + padding,
                    tileWidth - (padding * 2),
                    tileHeight - (padding * 2),
                    null);
        }

        // Draw health bar
        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getDurability(), entity.getMaxDurability());
    }

    // Draws a unit with its image and health bar
    private void drawUnit(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Unit entity) {
        Color unitColor = getPlayerColor(entity.getOwner().getName());
        // Draw shadow
        g2d.setColor(SHADOW_COLOR);
        g2d.fillOval(x * tileWidth + 7, y * tileHeight + 7, tileWidth - 10, tileHeight - 10);
        // Draw unit
        g2d.setColor(unitColor);
        g2d.fillOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6);
        g2d.setColor(unitColor.darker());
        g2d.drawOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6);

        // Draw unit image
        Image unitImage = null;
        if (entity instanceof Peasant) unitImage = peasantImage;
        else if (entity instanceof Spearman) unitImage = spearmanImage;
        else if (entity instanceof Swordsman) unitImage = swordsmanImage;
        else if (entity instanceof Knight) unitImage = knightImage;

        if (unitImage != null) {
            int padding = (int) (tileWidth * 0.15);
            g2d.drawImage(unitImage,
                    x * tileWidth + padding,
                    y * tileHeight + padding,
                    tileWidth - (padding * 2),
                    tileHeight - (padding * 2),
                    null);
        }

        // Draw health bar
        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getHealth(), entity.getMaxHealth());
    }

    // Draws a health bar for an entity
    private void drawHealthBar(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, int currentHealth, int maxHealth) {
        if (maxHealth <= 0) return;

        double healthPercent = (double) currentHealth / maxHealth;
        int barWidth = tileWidth - 14;
        int barHeight = 8;
        int barX = x * tileWidth + 7;
        int barY = y * tileHeight + tileHeight - barHeight - 5;

        // Draw background
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);

        // Draw health bar based on health percentage
        Color healthColor = (healthPercent > 0.6) ? new Color(34, 177, 76) : // Green
                (healthPercent > 0.3) ? new Color(255, 201, 14) : // Yellow
                        new Color(237, 28, 36); // Red

        g2d.setColor(healthColor);
        g2d.fillRoundRect(barX, barY, (int) (barWidth * healthPercent), barHeight, 5, 5);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 5, 5);
    }

    // Updates the panel with the current game board and selected entity
    public void updatePanel(GameBoard board, GameEntity selected) {
        this.gameBoard = board;
        this.selectedEntity = selected;
        if (this.selectedX == -1 && this.selectedY == -1) {
            this.selectedEntity = null;
        }
        repaint();
    }
}