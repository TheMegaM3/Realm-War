package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.units.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game board, containing a 2D grid of tiles.
 * This class manages the state of the board itself, but not the game rules.
 */
public class GameBoard {

    public final int width;
    public final int height;
    private final GameTile[][] tiles;

    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new GameTile[width][height];
        initializeBoard();
    }

    /**
     * Fills the board with an initial set of terrain tiles.
     */
    private void initializeBoard() {
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // 20% chance for a tile to be a ForestBlock.
                Block terrain = rand.nextDouble() < 0.20 ? new ForestBlock() : new EmptyBlock();
                tiles[x][y] = new GameTile(terrain);
            }
        }
    }

    /**
     * Retrieves the tile at the specified coordinates.
     * @return The GameTile object, or null if the coordinates are out of bounds.
     */
    public GameTile getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return null;
    }

    /**
     * Places a game entity onto a specific tile on the board.
     * @param entity The entity to place (can be null to clear a tile).
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void placeEntity(GameEntity entity, int x, int y) {
        GameTile tile = getTile(x, y);
        if (tile != null) {
            tile.setEntity(entity);
            if (entity != null) {
                // Ensure the entity's internal position matches its board position.
                entity.setPosition(x, y);
            }
        }
    }

    /**
     * Gets a list of all units on the board belonging to a specific player.
     */
    public List<Unit> getUnitsForPlayer(Player player) {
        List<Unit> playerUnits = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GameEntity e = tiles[x][y].getEntity();
                if (e instanceof Unit && e.getOwner() == player) {
                    playerUnits.add((Unit) e);
                }
            }
        }
        return playerUnits;
    }

    /**
     * Gets a list of all structures on the board belonging to a specific player.
     */
    public List<Structure> getStructuresForPlayer(Player player) {
        List<Structure> playerStructures = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GameEntity e = tiles[x][y].getEntity();
                if (e instanceof Structure && e.getOwner() == player) {
                    playerStructures.add((Structure) e);
                }
            }
        }
        return playerStructures;
    }

    /**
     * Gets a list of all units on the 8 tiles adjacent to the given coordinates.
     */
    public List<Unit> getAdjacentUnits(int x, int y) {
        List<Unit> adjacent = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1}; // All 8 directions (Moore neighborhood)
        int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
        for (int i = 0; i < 8; i++) {
            GameTile tile = getTile(x + dx[i], y + dy[i]);
            if (tile != null && tile.getEntity() instanceof Unit) {
                adjacent.add((Unit) tile.getEntity());
            }
        }
        return adjacent;
    }

    /**
     * Checks if a tile is adjacent (4 cardinal directions) to a friendly structure of a specific type.
     * Used to validate where units can be trained.
     */
    public boolean isAdjacentToFriendlyStructure(int x, int y, Player player, Class<? extends Structure> structureType) {
        int[] dx = {-1, 1, 0, 0}; // 4 cardinal directions (Von Neumann neighborhood)
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            GameTile tile = getTile(x + dx[i], y + dy[i]);
            if (tile != null && tile.getEntity() != null &&
                    structureType.isInstance(tile.getEntity()) &&
                    tile.getEntity().getOwner() == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Directly sets a tile on the board. Used for loading games.
     */
    public void setTile(int x, int y, GameTile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            this.tiles[x][y] = tile;
        }
    }
}
