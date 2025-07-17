package com.realmwar.model;

import com.realmwar.util.Constants;
import com.realmwar.util.CustomExceptions.GameRuleException;
import com.realmwar.model.structures.Barrack;
import com.realmwar.model.structures.TownHall;
import com.realmwar.engine.GameBoard;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Player {
    private Color color;
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

        this.color = switch (Math.abs(name.hashCode() % 4)) {
            case 0 -> new Color(173, 216, 230);
            case 1 -> new Color(255, 182, 193);
            case 2 -> new Color(221, 160, 221);
            case 3 -> new Color(216, 191, 216);
            default -> Color.GRAY;
        };
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
            case "Spearman" -> currentCount < Constants.MAX_SPEARMEN_PER_PLAYER;
            case "Swordsman" -> currentCount < Constants.MAX_SWORDSMEN_PER_PLAYER;
            case "Knight" -> currentCount < Constants.MAX_KNIGHTS_PER_PLAYER;
            default -> false;
        };
    }

    /**
     * Calculates the total unit space available from all Barracks and TownHall owned by the player.
     * @param gameBoard The game board to check for player's structures.
     * @return The total unit space available.
     */
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

    /**
     * Checks if the player has enough unit space to train a new unit.
     * @param gameBoard The game board to check for player's structures.
     * @return True if the player has enough unit space, false otherwise.
     */
    public boolean hasEnoughUnitSpace(GameBoard gameBoard) {
        int totalUnits = unitCounts.values().stream().mapToInt(Integer::intValue).sum();
        return totalUnits < getTotalUnitSpace(gameBoard);
    }

    public String getName() { return name; }
    public ResourceHandler getResourceHandler() { return resourceHandler; }
    public int getWins() { return wins; }
    public void incrementWins() { this.wins++; }
    public Color getColor() { return color; }
    public Map<String, Integer> getUnitCounts() { return unitCounts; }

    @Override
    public String toString() { return name; }
}