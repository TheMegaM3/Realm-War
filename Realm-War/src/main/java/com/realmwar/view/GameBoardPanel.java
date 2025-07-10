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
    private int selectedX = -1;
    private int selectedY = -1;
    private Unit attackingUnit;

    private static final Color PLAYER1_COLOR = new Color(30, 80, 150);
    private static final Color PLAYER2_COLOR = new Color(180, 40, 40);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);

    public GameBoardPanel(GameManager gameManager) {
        this.gameManager = gameManager;
        setBackground(new Color(101, 67, 33));
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tileWidth = getWidth() / gameBoard.width;
                int tileHeight = getHeight() / gameBoard.height;
                selectedX = e.getX() / tileWidth;
                selectedY = e.getY() / tileHeight;
                gameManager.setSelectedTile(selectedX, selectedY);
                selectedEntity = gameBoard.getTile(selectedX, selectedY).getEntity();

                if (attackingUnit != null) {
                    try {
                        gameManager.attackUnit(attackingUnit, selectedEntity);
                        attackingUnit = null;
                        updatePanel(gameManager.getGameBoard(), null);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GameBoardPanel.this,
                                ex.getMessage(), "Attack error", JOptionPane.ERROR_MESSAGE);
                    }
                }
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

    private void drawTile(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        Color baseColor = gameBoard.getTile(x, y).block.getColor();
        GradientPaint gp = new GradientPaint(
                x * tileWidth, y * tileHeight, baseColor.brighter(),
                x * tileWidth + tileWidth, y * tileHeight + tileHeight, baseColor.darker()
        );
        g2d.setPaint(gp);
        g2d.fillRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        g2d.setColor(new Color(70, 50, 30).darker());
        g2d.drawRect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

        GameEntity entity = gameBoard.getTile(x, y).getEntity();
        if (entity != null) {
            if (entity instanceof Unit) {
                drawUnit(g2d, x, y, tileWidth, tileHeight, entity);
            } else if (entity instanceof Structure) {
                drawStructure(g2d, x, y, tileWidth, tileHeight, entity);
            }
        }
    }

    private void drawStructure(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, GameEntity entity) {
        Color baseColor = entity.getOwner().getName().equals("Player 1")
                ? new Color(70, 130, 180) : new Color(220, 60, 60);

        g2d.setColor(SHADOW_COLOR);
        g2d.fillRoundRect(x * tileWidth + 5, y * tileHeight + 5, tileWidth - 10, tileHeight - 10, 15, 15);

        GradientPaint gp = new GradientPaint(
                x * tileWidth, y * tileHeight, baseColor.brighter(),
                x * tileWidth + tileWidth, y * tileHeight + tileHeight, baseColor.darker()
        );
        g2d.setPaint(gp);
        g2d.fillRoundRect(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 10, tileHeight - 10, 15, 15);

        String symbol = "";
        if (entity instanceof Farm) symbol = "🌾";
        else if (entity instanceof Barrack) symbol = "🛡️";
        else if (entity instanceof Market) symbol = "🏪";
        else if (entity instanceof Tower) symbol = "🏰";

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x * tileWidth + (tileWidth - fm.stringWidth(symbol)) / 2;
        int textY = y * tileHeight + ((tileHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(symbol, textX, textY);
    }

    private void drawUnit(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight, GameEntity entity) {
        Color unitColor = entity.getOwner().getName().equals("Player 1")
                ? PLAYER1_COLOR : PLAYER2_COLOR;

        g2d.setColor(SHADOW_COLOR);
        g2d.fillOval(x * tileWidth + 5, y * tileHeight + 5, tileWidth - 10, tileHeight - 10);

        GradientPaint gp = new GradientPaint(
                x * tileWidth, y * tileHeight, unitColor.brighter(),
                x * tileWidth + tileWidth, y * tileHeight + tileHeight, unitColor.darker()
        );
        g2d.setPaint(gp);
        g2d.fillOval(x * tileWidth + 3, y * tileHeight + 3, tileWidth - 10, tileHeight - 10);

        String swordSymbol = "";
        if (entity instanceof Peasant) swordSymbol = "⚔ 1";
        else if (entity instanceof Spearman) swordSymbol = "⚔ 2";
        else if (entity instanceof Swordsman) swordSymbol = "⚔ 3";
        else if (entity instanceof Knight) swordSymbol = "⚔ 4";

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x * tileWidth + (tileWidth - fm.stringWidth(swordSymbol)) / 2;
        int textY = y * tileHeight + ((tileHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(swordSymbol, textX, textY);
    }

    public void updatePanel(GameBoard board, GameEntity selected) {
        this.gameBoard = board;
        this.selectedEntity = selected;
        this.attackingUnit = null;
        this.selectedX = -1;
        this.selectedY = -1;
        repaint();
    }

    public void setAttackingUnit(Unit unit) {
        this.attackingUnit = unit;
        repaint();
    }


}