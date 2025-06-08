package com.realmwar.view;

import com.realmwar.engine.GameBoard;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.units.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * The panel that visually renders the entire game board.
 * This is a pure "View" component. It holds no game logic and only paints
 * the state of the model that the GameController provides to it.
 */
public class GameBoardPanel extends JPanel {

    private GameBoard gameBoard;
    private GameEntity selectedEntity;
    private final Map<String, BufferedImage> spriteCache = new HashMap<>();

    public GameBoardPanel() {
        // Pre-load all the sprites for our units and structures into memory.
        loadSprites();
    }

    /**
     * This public method is called by the GameController to pass the latest model
     * data to this panel for rendering.
     * @param board The current game board from the model.
     * @param selected The currently selected entity, for highlighting.
     */
    public void updatePanel(GameBoard board, GameEntity selected) {
        this.gameBoard = board;
        this.selectedEntity = selected;
        // The repaint() call schedules this component to be redrawn.
        repaint();
    }

    /**
     * The core rendering method of the panel. It is called automatically
     * by Swing whenever the component needs to be redrawn (e.g., after repaint()).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameBoard == null) return; // Don't draw if there's no board data yet.

        Graphics2D g2d = (Graphics2D) g;
        // Make the drawing smoother
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate tile size based on current window size, making the board responsive.
        int tileWidth = getWidth() / gameBoard.width;
        int tileHeight = getHeight() / gameBoard.height;

        // Loop through every tile of the board model and draw it.
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                drawTile(g2d, x, y, tileWidth, tileHeight);
            }
        }
    }

    /**
     * A helper method to draw a single tile and everything on it.
     * This keeps the main paintComponent method clean.
     */
    private void drawTile(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        int tileX = x * tileWidth;
        int tileY = y * tileHeight;

        // 1. Draw the terrain using the polymorphic Block's getColor method.
        g2d.setColor(gameBoard.getTile(x, y).block.getColor());
        g2d.fillRect(tileX, tileY, tileWidth, tileHeight);

        // 2. Draw the entity (if any) on the tile.
        GameEntity entity = gameBoard.getTile(x, y).getEntity();
        if (entity != null) {
            drawEntity(g2d, entity, tileX, tileY, tileWidth, tileHeight);
        }

        // 3. Draw a highlight if this tile's entity is selected.
        if (selectedEntity != null && selectedEntity.getX() == x && selectedEntity.getY() == y) {
            drawSelectionHighlight(g2d, tileX, tileY, tileWidth, tileHeight);
        }

        // 4. Draw the grid lines over everything.
        g2d.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black for a softer grid
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(tileX, tileY, tileWidth, tileHeight);
    }

    /**
     * Draws the visual representation of a unit or structure.
     */
    private void drawEntity(Graphics2D g2d, GameEntity entity, int x, int y, int w, int h) {
        // Get the pre-rendered sprite from our cache.
        BufferedImage sprite = spriteCache.get(entity.getClass().getSimpleName());
        if (sprite != null) {
            g2d.drawImage(sprite, x, y, w, h, null);
        }

        // Draw a small colored bar to indicate the owner.
        drawOwnerIndicator(g2d, entity.getOwner(), x, y, w);

        // If the entity is a Unit or Structure, draw its health bar.
        if (entity instanceof Unit) {
            drawHealthBar(g2d, ((Unit) entity).getHealth(), ((Unit) entity).getMaxHealth(), x, y, w, h);
        } else if (entity instanceof Structure) {
            drawHealthBar(g2d, ((Structure) entity).getDurability(), ((Structure) entity).getMaxDurability(), x, y, w, h);
        }
    }

    private void drawOwnerIndicator(Graphics2D g, Player owner, int x, int y, int w) {
        // Player 1 gets a blue bar, Player 2 gets a red bar.
        g.setColor(owner.getName().equals("Player 1") ? new Color(50, 100, 255) : new Color(255, 50, 50));
        g.fillRect(x + 3, y + 3, w / 4, 6);
    }

    private void drawHealthBar(Graphics2D g, int currentHealth, int maxHealth, int x, int y, int w, int h) {
        double healthPercent = (double) currentHealth / maxHealth;

        // Health bar background
        g.setColor(new Color(0, 0, 0, 150));
        g.fill(new Rectangle2D.Double(x + 4, y + h - 12, w - 8, 8));

        // Health bar foreground (changes color based on health)
        g.setColor(healthPercent > 0.5 ? Color.GREEN : (healthPercent > 0.25 ? Color.ORANGE : Color.RED));
        g.fill(new Rectangle2D.Double(x + 4, y + h - 12, (w - 8) * healthPercent, 8));
    }

    private void drawSelectionHighlight(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Color.YELLOW);
        g.setStroke(new BasicStroke(3)); // A thick stroke to make it stand out
        g.drawRect(x + 1, y + 1, w - 3, h - 3); // Draw slightly inside the tile
    }

    /**
     * Creates and caches all the sprites for our game entities.
     * This uses dynamic drawing, so no image files are needed.
     */
    private void loadSprites() {
        spriteCache.put("Peasant", createPlaceholderSprite("P", new Color(220, 220, 220)));
        spriteCache.put("Spearman", createPlaceholderSprite("Sp", new Color(180, 180, 255)));
        spriteCache.put("Swordsman", createPlaceholderSprite("Sw", new Color(255, 180, 180)));
        spriteCache.put("Knight", createPlaceholderSprite("K", new Color(255, 215, 0)));
        spriteCache.put("TownHall", createPlaceholderSprite("TH", new Color(218, 165, 32)));
        spriteCache.put("Barrack", createPlaceholderSprite("B", new Color(139, 69, 19)));
        spriteCache.put("Farm", createPlaceholderSprite("F", new Color(152, 251, 152)));
        spriteCache.put("Market", createPlaceholderSprite("M", new Color(175, 238, 238)));
        spriteCache.put("Tower", createPlaceholderSprite("T", new Color(160, 160, 160)));
    }

    private BufferedImage createPlaceholderSprite(String text, Color color) {
        // Create a 64x64 pixel image in memory with transparency
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Make the drawing look nice
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the main colored circle
        g2d.setColor(color);
        g2d.fillOval(2, 2, 60, 60);

        // Draw a black border around it
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(2, 2, 60, 60);

        // Draw the text in the center
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, (64 - fm.stringWidth(text)) / 2, (64 - fm.getHeight()) / 2 + fm.getAscent());

        g2d.dispose(); // Free up graphics resources
        return img;
    }
}
