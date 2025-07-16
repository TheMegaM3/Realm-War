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

public class GameBoardPanel extends JPanel {
    private GameBoard gameBoard;
    private GameEntity selectedEntity;
    private final GameManager gameManager;
    private final GameFrame gameFrame;
    private int selectedX = -1;
    private int selectedY = -1;

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
    private List<Point> movableTiles = new ArrayList<>();

    public GameBoardPanel(GameManager gameManager, GameFrame frame) {
        this.gameManager = gameManager;
        this.gameFrame = frame;

        setBackground(new Color(250, 240, 230));
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));

        loadAssetImages();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameBoard == null) return;

                Insets insets = getInsets();
                int drawableWidth = getWidth() - insets.left - insets.right;
                int drawableHeight = getHeight() - insets.top - insets.bottom;

                if (drawableWidth <= 0 || drawableHeight <= 0) return;

                int tileWidth = drawableWidth / gameBoard.width;
                int tileHeight = drawableHeight / gameBoard.height;

                int mouseX = e.getX() - insets.left;
                int mouseY = e.getY() - insets.top;

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

    private void handleGameActions(GameEntity clickedEntity) {
        GameTile clickedTile = gameBoard.getTile(selectedX, selectedY);
        if (clickedTile == null) return;

        if (gameFrame.isMergeModeActive()) {
            if (clickedEntity instanceof Unit && clickedEntity.getOwner() == gameManager.getCurrentPlayer()) {
                gameFrame.handleMergeClick((Unit) clickedEntity);
            } else {
                JOptionPane.showMessageDialog(this, "Please select one of your units.", "Merge Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        Unit currentSelectedUnit = gameManager.getSelectedUnit();
        if (currentSelectedUnit == null) {
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

    private void clearHighlights() {
        movableTiles.clear();
        repaint();
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameBoard == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = getInsets();
        int drawableWidth = getWidth() - insets.left - insets.right;
        int drawableHeight = getHeight() - insets.top - insets.bottom;

        if (drawableWidth <= 0 || drawableHeight <= 0) return;

        int tileWidth = drawableWidth / gameBoard.width;
        int tileHeight = drawableHeight / gameBoard.height;

        g.translate(insets.left, insets.top);

        // Draw tiles
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                drawTile(g2d, x, y, tileWidth, tileHeight);
            }
        }

        if (selectedX >= 0 && selectedY >= 0) {
            g2d.setColor(new Color(255, 223, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(selectedX * tileWidth, selectedY * tileHeight, tileWidth, tileHeight);
        }

        Unit selectedUnit = gameManager.getSelectedUnit();
        if (selectedUnit != null) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(selectedUnit.getX() * tileWidth, selectedUnit.getY() * tileHeight, tileWidth, tileHeight);
        }

        g2d.setColor(new Color(144, 238, 144, 128));
        for (Point p : movableTiles) {
            int drawX = p.x * tileWidth;
            int drawY = p.y * tileHeight;
            g2d.fillRect(drawX, drawY, tileWidth, tileHeight);
        }

        g.translate(-insets.left, -insets.top);
    }

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

    private void drawTile(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        Color baseColor = gameBoard.getTile(x, y).block.getColor();
        g2d.setColor(baseColor);
        g2d.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        g2d.setColor(baseColor.darker().darker());
        g2d.drawRect(x * tileWidth, y * tileHeight, tileWidth -1, tileHeight -1);

        GameEntity entity = gameBoard.getTile(x, y).getEntity();
        if (entity != null) {
            if (entity instanceof Unit unit) {
                drawUnit(g2d, x, y, tileWidth, tileHeight, unit);
            } else if (entity instanceof Structure structure) {
                drawStructure(g2d, x, y, tileWidth, tileHeight, structure);
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

        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getDurability(), entity.getMaxDurability());
    }

    private void drawUnit(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Unit entity) {
        Color unitColor = getPlayerColor(entity.getOwner().getName());
        g2d.setColor(SHADOW_COLOR);
        g2d.fillOval(x * tileWidth + 7, y * tileHeight + 7, tileWidth - 10, tileHeight - 10);
        g2d.setColor(unitColor);
        g2d.fillOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6);
        g2d.setColor(unitColor.darker());
        g2d.drawOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 6, tileHeight - 6);

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

        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getHealth(), entity.getMaxHealth());
    }

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

        g2d.setColor(healthColor);
        g2d.fillRoundRect(barX, barY, (int) (barWidth * healthPercent), barHeight, 5, 5);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 5, 5);
    }

    public void updatePanel(GameBoard board, GameEntity selected) {
        this.gameBoard = board;
        this.selectedEntity = selected;
        if (this.selectedX == -1 && this.selectedY == -1) {
            this.selectedEntity = null;
        }
        repaint();
    }
}