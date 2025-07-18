// GameBoard.java
// Represents the game board in the RealmWar game, managing a grid of tiles with terrain and entities.
// Handles board initialization, entity placement, and territory management for players.

package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Barrack;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.structures.Tower;
import com.realmwar.model.units.Unit;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Class representing the game board with a grid of tiles
public class GameBoard {
    // Board dimensions
    public final int width;
    public final int height;
    // 2D array of tiles representing the game board
    private final GameTile[][] tiles;

    // Constructor to initialize the board with specified dimensions
    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new GameTile[width][height];
        initializeBoard();
    }

    // Initializes the board with random terrain (ForestBlock or EmptyBlock)
    private void initializeBoard() {
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Block terrain = rand.nextDouble() < 0.20 ? new ForestBlock() : new EmptyBlock();
                tiles[x][y] = new GameTile(terrain, x, y);
            }
        }
    }

    // Retrieves a tile at the specified coordinates, or null if out of bounds
    public GameTile getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return null;
    }

    // Places an entity on the specified tile and updates its position
    public void placeEntity(GameEntity entity, int x, int y) {
        GameTile tile = getTile(x, y);
        if (tile != null) {
            tile.setEntity(entity);
            if (entity != null) {
                entity.setPosition(x, y);
            }
        }
    }

    // Returns a list of all units owned by the specified player
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

    // Returns a list of all structures owned by the specified player
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

    // Returns a list of units adjacent to the specified coordinates
    public List<Unit> getAdjacentUnits(int x, int y) {
        List<Unit> adjacent = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
        for (int i = 0; i < 8; i++) {
            GameTile tile = getTile(x + dx[i], y + dy[i]);
            if (tile != null && tile.getEntity() instanceof Unit) {
                adjacent.add((Unit) tile.getEntity());
            }
        }
        return adjacent;
    }

    // Checks if the specified coordinates are adjacent to a friendly structure of the given type
    public boolean isAdjacentToFriendlyStructure(int x, int y, Player player, Class<? extends Structure> structureType) {
        for (Structure structure : getStructuresForPlayer(player)) {
            if (structureType.isInstance(structure)) {
                if (structure instanceof Barrack barrack) {
                    List<Point> validDirections = barrack.getValidUnitPlacementDirections();
                    for (Point direction : validDirections) {
                        int checkX = structure.getX() + direction.x;
                        int checkY = structure.getY() + direction.y;
                        if (checkX == x && checkY == y) {
                            return true;
                        }
                    }
                } else {
                    int[] dx = {-1, 1, 0, 0};
                    int[] dy = {0, 0, -1, 1};
                    for (int i = 0; i < 4; i++) {
                        int checkX = structure.getX() + dx[i];
                        int checkY = structure.getY() + dy[i];
                        if (checkX == x && checkY == y) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Checks if the specified coordinates are adjacent to a friendly tower
    public boolean isAdjacentToFriendlyTower(int x, int y, Player player) {
        int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
        for (int i = 0; i < 8; i++) {
            GameTile tile = getTile(x + dx[i], y + dy[i]);
            if (tile != null && tile.getEntity() instanceof Tower &&
                    tile.getEntity().getOwner() == player) {
                return true;
            }
        }
        return false;
    }

    // Sets a tile at the specified coordinates
    public void setTile(int x, int y, GameTile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            this.tiles[x][y] = tile;
        }
    }

    // Sets the territory owner for a tile at the specified coordinates
    public void setTerritory(Player player, int x, int y) {
        GameTile tile = getTile(x, y);
        if (tile != null) {
            tile.setTerritoryOwner(player);
        }
    }

    // Initializes a player's territory around a central point
    public void initializePlayerTerritory(Player player, int centerX, int centerY) {
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    setTerritory(player, x, y);
                }
            }
        }
    }
}