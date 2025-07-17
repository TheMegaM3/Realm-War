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

    private void initializeBoard() {
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Block terrain = rand.nextDouble() < 0.20 ? new ForestBlock() : new EmptyBlock();
                tiles[x][y] = new GameTile(terrain, x, y);
            }
        }
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
            }
        }
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

    public void setTerritory(Player player, int x, int y) {
        GameTile tile = getTile(x, y);
        if (tile != null) {
            tile.setTerritoryOwner(player);
        }
    }

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