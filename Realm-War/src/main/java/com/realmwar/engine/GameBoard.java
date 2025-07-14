package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
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
    private final List<DynamicTerritory> territories;

    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new GameTile[width][height];
        this.territories = new ArrayList<>();
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

    public void initializeTerritories(List<String> playerNames) {
        territories.clear();
        int territorySize = 3;
        int halfSize = territorySize / 2;

        List<int[]> townHallPositions = new ArrayList<>();
        if (playerNames.size() == 2) {
            townHallPositions.add(new int[]{halfSize, halfSize});
            townHallPositions.add(new int[]{width - halfSize - 1, height - halfSize - 1});
        } else if (playerNames.size() == 3) {
            townHallPositions.add(new int[]{halfSize, halfSize});
            townHallPositions.add(new int[]{width - halfSize - 1, height - halfSize - 1});
            townHallPositions.add(new int[]{width - halfSize - 1, halfSize});
        } else if (playerNames.size() >= 4) {
            townHallPositions.add(new int[]{halfSize, halfSize});
            townHallPositions.add(new int[]{width - halfSize - 1, height - halfSize - 1});
            townHallPositions.add(new int[]{width - halfSize - 1, halfSize});
            townHallPositions.add(new int[]{halfSize, height - halfSize - 1});
        }

        for (int i = 0; i < playerNames.size(); i++) {
            int centerX = townHallPositions.get(i)[0];
            int centerY = townHallPositions.get(i)[1];
            DynamicTerritory territory = new DynamicTerritory(playerNames.get(i));
            for (int x = Math.max(0, centerX - halfSize); x <= Math.min(width - 1, centerX + halfSize); x++) {
                for (int y = Math.max(0, centerY - halfSize); y <= Math.min(height - 1, centerY + halfSize); y++) {
                    territory.addTile(new Point(x, y));
                }
            }
            territories.add(territory);
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
        int[] dx = {-1, 1, 0, 0};
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

    public boolean isWithinOrAdjacentToTerritory(int x, int y, Player player) {
        DynamicTerritory territory = getTerritoryForPlayer(player.getName());
        if (territory == null) return false;

        boolean isWithin = territory.getTiles().contains(new Point(x, y));
        if (!isWithin) {
            int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
            int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
            for (int i = 0; i < 8; i++) {
                if (territory.getTiles().contains(new Point(x + dx[i], y + dy[i]))) {
                    return true;
                }
            }
        }
        return isWithin;
    }

    public void addTileToTerritory(Player player, Point tile) {
        DynamicTerritory territory = getTerritoryForPlayer(player.getName());
        if (territory != null) {
            for (DynamicTerritory other : territories) {
                if (!other.getPlayerName().equals(player.getName())) {
                    other.removeTile(tile);
                }
            }
            territory.addTile(tile);
            GameTile gameTile = getTile(tile.x, tile.y);
            if (gameTile != null) {
                gameTile.setOwner(player);
            }
        }
    }

    public void removeTerritory(String playerName) {
        territories.removeIf(t -> t.getPlayerName().equals(playerName));
    }

    public DynamicTerritory getTerritoryForPlayer(String playerName) {
        return territories.stream()
                .filter(t -> t.getPlayerName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    public List<DynamicTerritory> getTerritories() {
        return territories;
    }

    public void setTile(int x, int y, GameTile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            this.tiles[x][y] = tile;
        }
    }

    public static class DynamicTerritory {
        private final String playerName;
        private final List<Point> tiles;

        public DynamicTerritory(String playerName) {
            this.playerName = playerName;
            this.tiles = new ArrayList<>();
        }

        public void addTile(Point tile) {
            if (!tiles.contains(tile)) {
                tiles.add(tile);
            }
        }

        public void removeTile(Point tile) {
            tiles.remove(tile);
        }

        public List<Point> getTiles() {
            return tiles;
        }

        public String getPlayerName() {
            return playerName;
        }
    }
}