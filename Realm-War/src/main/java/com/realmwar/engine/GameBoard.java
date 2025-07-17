package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.structures.Tower;
import com.realmwar.model.units.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameBoard {
    public final int width;
    public final int height;
    private final GameTile[][] tiles;

    public GameBoard(int width, int height, List<Player> players) {
        this.width = width;
        this.height = height;
        this.tiles = new GameTile[width][height];
        initializeBoard();
    }

    private void initializeBoard() {
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Block terrain = rand.nextDouble() < 0.20 ? new ForestBlock() : new EmptyBlock();
                // MODIFIED: Calling the corrected GameTile constructor.
                tiles[x][y] = new GameTile(terrain, x, y);
            }
        }
    }

    public long getTerritorySize(Player player) {
        long count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y].getOwner() == player) {
                    count++;
                }
            }
        }
        return count;
    }

    public GameTile getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return null;
    }

    public void placeEntity(GameEntity entity, int x, int y) {
        GameTile tile = getTile(x, y);
        if (tile != null) {
            tile.setEntity(entity);
            if (entity != null) {
                entity.setPosition(x, y);
                tile.setOwner(entity.getOwner());
            }
        }
    }

    public boolean isAdjacentToTerritory(int x, int y, Player player) {
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            GameTile adjacentTile = getTile(x + dx[i], y + dy[i]);
            if (adjacentTile != null && adjacentTile.getOwner() == player) {
                return true;
            }
        }
        return false;
    }

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

    public List<Unit> getUnitsInRadius(int centerX, int centerY, int radius) {
        List<Unit> units = new ArrayList<>();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (Math.abs(x - centerX) + Math.abs(y - centerY) <= radius) {
                    GameTile tile = getTile(x, y);
                    if (tile != null && tile.getEntity() instanceof Unit) {
                        units.add((Unit) tile.getEntity());
                    }
                }
            }
        }
        return units;
    }

    public boolean isAdjacentToFriendlyStructure(int x, int y, Player player, Class<? extends Structure> structureType) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int checkX = x + dx[i];
            int checkY = y + dy[i];
            GameTile tile = getTile(checkX, checkY);
            if (tile != null && tile.getEntity() != null &&
                    structureType.isInstance(tile.getEntity()) && tile.getEntity().getOwner() == player) {
                return true;
            }
        }
        return false;
    }

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

    public void setTile(int x, int y, GameTile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            this.tiles[x][y] = tile;
        }
    }
}
