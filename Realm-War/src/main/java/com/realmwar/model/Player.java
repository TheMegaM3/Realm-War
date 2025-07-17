package com.realmwar.model;

import com.realmwar.engine.GameBoard;
import com.realmwar.model.structures.Barrack;
import com.realmwar.model.structures.TownHall;
import com.realmwar.util.Constants;

import java.util.Map;
import java.util.HashMap;

public class Player {
    private final String name;
    private final ResourceHandler resourceHandler;
    private int wins;
    private final Map<String, Integer> unitCounts;

    public Player(String name, int startingGold, int startingFood) {
        this.name = name;
        this.resourceHandler = new ResourceHandler(startingGold, startingFood);
        this.wins = 0;
        this.unitCounts = new HashMap<>();
        this.unitCounts.put("Peasant", 0);
        this.unitCounts.put("Spearman", 0);
        this.unitCounts.put("Swordsman", 0);
        this.unitCounts.put("Knight", 0);
    }

    public void incrementUnitCount(String unitType) {
        unitCounts.put(unitType, unitCounts.getOrDefault(unitType, 0) + 1);
    }

    public void decrementUnitCount(String unitType) {
        unitCounts.put(unitType, Math.max(0, unitCounts.getOrDefault(unitType, 0) - 1));
    }

    public boolean canTrainUnit(String unitType) {
        int currentCount = unitCounts.getOrDefault(unitType, 0);
        return switch (unitType) {
            case "Peasant" -> currentCount < Constants.MAX_PEASANTS_PER_PLAYER;
            case "Spearman" -> currentCount < Constants.MAX_SPEARMAN_PER_PLAYER;
            case "Swordsman" -> currentCount < Constants.MAX_SWORDSMEN_PER_PLAYER;
            case "Knight" -> currentCount < Constants.MAX_KNIGHTS_PER_PLAYER;
            default -> false;
        };
    }

    public int getTotalUnitSpace(GameBoard gameBoard) {
        int totalUnitSpace = 0;
        for (var structure : gameBoard.getStructuresForPlayer(this)) {
            if (structure instanceof TownHall) {
                totalUnitSpace += Constants.TOWNHALL_UNIT_SPACE;
            } else if (structure instanceof Barrack barrack) {
                totalUnitSpace += barrack.getUnitSpace();
            }
        }
        return totalUnitSpace;
    }

    public boolean hasEnoughUnitSpace(GameBoard gameBoard) {
        return getCurrentUnitCount() < getUnitCapacity(gameBoard);
    }

    // MODIFIED: Added this method to fix the compilation error.
    public int getCurrentUnitCount() {
        return unitCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    // MODIFIED: Added this method to fix the compilation error. It's an alias for getTotalUnitSpace.
    public int getUnitCapacity(GameBoard gameBoard) {
        return getTotalUnitSpace(gameBoard);
    }

    public String getName() { return name; }
    public ResourceHandler getResourceHandler() { return resourceHandler; }
    public int getWins() { return wins; }
    public void incrementWins() { this.wins++; }
    public Map<String, Integer> getUnitCounts() { return unitCounts; }

    @Override
    public String toString() { return name; }
}
