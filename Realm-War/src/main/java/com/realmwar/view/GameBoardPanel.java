package com.realmwar.view;

import com.realmwar.engine.GameBoard;
import com.realmwar.engine.GameManager;
import com.realmwar.engine.GameTile;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;
import com.realmwar.util.CustomExceptions;

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
    private final GameManager gameManager;
    private final GameFrame gameFrame;

    private Image peasantImage, spearmanImage, swordsmanImage, knightImage;
    private Image farmImage, barrackImage, marketImage, townhallImage, towerImage;

    private List<Point> movableTiles = new ArrayList<>();
    private List<Point> attackableTiles = new ArrayList<>();

    public GameBoardPanel(GameManager gameManager, GameFrame frame) {
        this.gameManager = gameManager;
        this.gameFrame = frame;
        this.gameBoard = gameManager.getGameBoard();

        setBackground(new Color(250, 240, 230));
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
        loadAssetImages();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameBoard == null) return;
                Point tileCoords = getTileCoordinatesFromMouseEvent(e);
                if (tileCoords != null) {
                    handleTileClick(tileCoords.x, tileCoords.y);
                }
            }
        });
    }

    private Point getTileCoordinatesFromMouseEvent(MouseEvent e) {
        Insets insets = getInsets();
        int drawableWidth = getWidth() - insets.left - insets.right;
        int drawableHeight = getHeight() - insets.top - insets.bottom;

        if (drawableWidth <= 0 || drawableHeight <= 0) return null;

        int tileWidth = drawableWidth / gameBoard.width;
        int tileHeight = drawableHeight / gameBoard.height;

        int mouseX = e.getX() - insets.left;
        int mouseY = e.getY() - insets.top;

        if (mouseX >= 0 && mouseX < drawableWidth && mouseY >= 0 && mouseY < drawableHeight) {
            return new Point(mouseX / tileWidth, mouseY / tileHeight);
        }
        return null;
    }

    private void handleTileClick(int x, int y) {
        gameManager.setSelectedTile(x, y);
        GameTile clickedTile = gameBoard.getTile(x, y);
        if (clickedTile == null) return;

        Unit selectedUnit = gameManager.getSelectedUnit();

        if (gameFrame.isMergeModeActive()) {
            if (clickedTile.getEntity() instanceof Unit unit && unit.getOwner() == gameManager.getCurrentPlayer()) {
                gameFrame.handleMergeClick(unit);
            } else {
                JOptionPane.showMessageDialog(this, "Merge Mode: Please select one of your own units.", "Merge Error", JOptionPane.ERROR_MESSAGE);
            }
            repaint();
            return;
        }

        if (selectedUnit == null) {
            if (clickedTile.getEntity() instanceof Unit unit && unit.getOwner() == gameManager.getCurrentPlayer()) {
                if (!unit.hasActedThisTurn()) {
                    gameManager.setSelectedUnit(unit);
                    highlightPossibleActions(unit);
                } else {
                    JOptionPane.showMessageDialog(this, "This unit has already acted this turn.", "Action Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            GameEntity targetEntity = clickedTile.getEntity();

            if (targetEntity != null && targetEntity.getOwner() != gameManager.getCurrentPlayer() && isTileAttackable(x, y)) {
                try {
                    gameManager.attackUnit(selectedUnit, targetEntity);
                    clearSelectionAndHighlights();
                    gameFrame.updateView();
                } catch (CustomExceptions.GameRuleException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Attack Error", JOptionPane.ERROR_MESSAGE);
                    clearSelectionAndHighlights();
                }
            }
            else if (targetEntity == null && isTileMovable(x, y)) {
                try {
                    gameManager.moveUnit(selectedUnit, x, y);
                    clearSelectionAndHighlights();
                    gameFrame.updateView();
                } catch (CustomExceptions.GameRuleException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Move Error", JOptionPane.ERROR_MESSAGE);
                    clearSelectionAndHighlights();
                }
            }
            else {
                clearSelectionAndHighlights();
            }
        }
        repaint();
    }

    private void highlightPossibleActions(Unit unit) {
        clearHighlights();
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                if (unit.canMoveTo(gameBoard.getTile(x, y), gameBoard)) {
                    movableTiles.add(new Point(x, y));
                }
            }
        }
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                GameEntity target = gameBoard.getTile(x, y).getEntity();
                int distance = Math.abs(unit.getX() - x) + Math.abs(unit.getY() - y);
                if (target != null && target.getOwner() != unit.getOwner() && distance <= unit.getAttackRange()) {
                    attackableTiles.add(new Point(x, y));
                }
            }
        }
    }

    private void clearSelectionAndHighlights() {
        gameManager.setSelectedUnit(null);
        gameManager.setSelectedTile(-1, -1);
        clearHighlights();
    }

    private void clearHighlights() {
        movableTiles.clear();
        attackableTiles.clear();
    }

    private boolean isTileMovable(int x, int y) {
        return movableTiles.stream().anyMatch(p -> p.x == x && p.y == y);
    }

    private boolean isTileAttackable(int x, int y) {
        return attackableTiles.stream().anyMatch(p -> p.x == x && p.y == y);
    }

    private void loadAssetImages() {
        try {
            peasantImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/peasant2.png")));
            spearmanImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/spearman1.png")));
            swordsmanImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/swordsman2.png")));
            knightImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/knight2.png")));
            townhallImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/townhall2.png")));
            farmImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/farm2.png")));
            barrackImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/barrack2.png")));
            marketImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/market2.png")));
            towerImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/tower2.png")));
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            System.err.println("Error loading asset images. Make sure all images are in the src/main/resources/assets folder.");
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

        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                drawTile(g2d, x, y, tileWidth, tileHeight);
            }
        }

        drawActionHighlights(g2d, tileWidth, tileHeight);

        int[] selectedTile = gameManager.getSelectedTile();
        if (selectedTile[0] != -1) {
            drawSelectionHighlight(g2d, selectedTile[0], selectedTile[1], tileWidth, tileHeight);
        }

        g.translate(-insets.left, -insets.top);
    }

    private void drawTile(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        GameTile tile = gameBoard.getTile(x, y);
        Player owner = tile.getOwner();

        if (owner != null) {
            g2d.setColor(getPlayerSolidColor(owner.getName()));
        } else {
            // MODIFIED: Use getter for block property
            g2d.setColor(tile.getBlock().getColor());
        }
        g2d.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        // MODIFIED: Use getter for block property
        g2d.setColor(tile.getBlock().getColor().darker());
        g2d.drawRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        drawTerritoryBorders(g2d, x, y, tileWidth, tileHeight);

        GameEntity entity = tile.getEntity();
        if (entity != null) {
            if (entity instanceof Unit unit) {
                drawUnit(g2d, x, y, tileWidth, tileHeight, unit);
            } else if (entity instanceof Structure structure) {
                drawStructure(g2d, x, y, tileWidth, tileHeight, structure);
            }
        }
    }

    private void drawTerritoryBorders(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        Player owner = gameBoard.getTile(x, y).getOwner();
        if (owner == null) return;

        g2d.setColor(getPlayerSolidColor(owner.getName()).darker());
        g2d.setStroke(new BasicStroke(1));

        GameTile top = gameBoard.getTile(x, y - 1);
        if (top == null || top.getOwner() != owner) {
            g2d.drawLine(x * tileWidth, y * tileHeight, (x + 1) * tileWidth, y * tileHeight);
        }
        GameTile bottom = gameBoard.getTile(x, y + 1);
        if (bottom == null || bottom.getOwner() != owner) {
            g2d.drawLine(x * tileWidth, (y + 1) * tileHeight, (x + 1) * tileWidth, (y + 1) * tileHeight);
        }
        GameTile left = gameBoard.getTile(x - 1, y);
        if (left == null || left.getOwner() != owner) {
            g2d.drawLine(x * tileWidth, y * tileHeight, x * tileWidth, (y + 1) * tileHeight);
        }
        GameTile right = gameBoard.getTile(x + 1, y);
        if (right == null || right.getOwner() != owner) {
            g2d.drawLine((x + 1) * tileWidth, y * tileHeight, (x + 1) * tileWidth, (y + 1) * tileHeight);
        }
    }

    private void drawActionHighlights(Graphics2D g2d, int tileWidth, int tileHeight) {
        g2d.setColor(new Color(144, 238, 144, 128));
        for (Point p : movableTiles) {
            g2d.fillRect(p.x * tileWidth, p.y * tileHeight, tileWidth, tileHeight);
        }
        g2d.setColor(new Color(255, 105, 97, 128));
        for (Point p : attackableTiles) {
            g2d.fillRect(p.x * tileWidth, p.y * tileHeight, tileWidth, tileHeight);
        }
    }

    private void drawSelectionHighlight(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(dashed);
        g2d.drawRect(x * tileWidth + 2, y * tileHeight + 2, tileWidth - 4, tileHeight - 4);
    }

    private Color getPlayerSolidColor(String playerName) {
        if (playerName == null) return Color.GRAY;
        return switch (playerName) {
            case "Player 1" -> new Color(173, 216, 230);
            case "Player 2" -> new Color(255, 105, 97);
            case "Player 3" -> new Color(255, 209, 220);
            case "Player 4" -> new Color(204, 153, 204);
            default -> Color.GRAY;
        };
    }

    private void drawStructure(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Structure entity) {
        Image structureImage = null;
        if (entity instanceof TownHall) structureImage = townhallImage;
        else if (entity instanceof Farm) structureImage = farmImage;
        else if (entity instanceof Barrack) structureImage = barrackImage;
        else if (entity instanceof Market) structureImage = marketImage;
        else if (entity instanceof Tower) structureImage = towerImage;

        if (structureImage != null) {
            int padding = (int) (tileWidth * 0.1);
            g2d.drawImage(structureImage, x * tileWidth + padding, y * tileHeight + padding, tileWidth - (padding * 2), tileHeight - (padding * 2), null);
        }

        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getDurability(), entity.getMaxDurability());

        if (entity.getLevel() > 1) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("L" + entity.getLevel(), x * tileWidth + 5, y * tileHeight + 15);
        }
    }

    private void drawUnit(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, Unit entity) {
        Color unitColor = getPlayerSolidColor(entity.getOwner().getName());
        g2d.setColor(unitColor.brighter());
        g2d.fillOval(x * tileWidth + 4, y * tileHeight + 4, tileWidth - 8, tileHeight - 8);
        g2d.setColor(unitColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x * tileWidth + 4, y * tileHeight + 4, tileWidth - 8, tileHeight - 8);

        Image unitImage = null;
        if (entity instanceof Peasant) unitImage = peasantImage;
        else if (entity instanceof Spearman) unitImage = spearmanImage;
        else if (entity instanceof Swordsman) unitImage = swordsmanImage;
        else if (entity instanceof Knight) unitImage = knightImage;

        if (unitImage != null) {
            int padding = (int) (tileWidth * 0.15);
            g2d.drawImage(unitImage, x * tileWidth + padding, y * tileHeight + padding, tileWidth - (padding * 2), tileHeight - (padding * 2), null);
        }

        if (entity.hasActedThisTurn()) {
            g2d.setColor(new Color(40, 40, 40, 150));
            g2d.fillOval(x * tileWidth + 4, y * tileHeight + 4, tileWidth - 8, tileHeight - 8);
        }

        drawHealthBar(g2d, x, y, tileWidth, tileHeight, entity.getHealth(), entity.getMaxHealth());
    }

    private void drawHealthBar(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, int currentHealth, int maxHealth) {
        if (maxHealth <= 0 || currentHealth == maxHealth) return;

        double healthPercent = (double) currentHealth / maxHealth;
        int barWidth = tileWidth - 14;
        int barHeight = 8;
        int barX = x * tileWidth + 7;
        int barY = y * tileHeight + tileHeight - barHeight - 5;

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);

        Color healthColor = (healthPercent > 0.6) ? new Color(34, 177, 76) : (healthPercent > 0.3) ? new Color(255, 201, 14) : new Color(237, 28, 36);

        g2d.setColor(healthColor);
        g2d.fillRoundRect(barX, barY, (int) (barWidth * healthPercent), barHeight, 5, 5);

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 5, 5);
    }

    public void updatePanel() {
        this.gameBoard = gameManager.getGameBoard();
        repaint();
    }
}
