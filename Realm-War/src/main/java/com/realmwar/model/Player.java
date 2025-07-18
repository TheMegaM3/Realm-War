// Player.java
// Represents a player in the RealmWar game, managing their resources, units, and attributes.
// Tracks unit counts, resource handler, and visual representation (color).

package com.realmwar.model;

import com.realmwar.util.Constants;
import com.realmwar.util.CustomExceptions.GameRuleException;
import com.realmwar.model.structures.Barrack;
import com.realmwar.model.structures.TownHall;
import com.realmwar.engine.GameBoard;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// Class representing a player in the game
public class Player {
    // The player's color for visual representation
    private Color color;
    // The player's name
    private final String name;
    // Manages the player's resources (gold and food)
    private final ResourceHandler resourceHandler;
    // Number of wins the player has achieved
    private int wins;
    // Tracks the count of each unit type owned by the player
    private final Map<String, Integer> unitCounts;

    // Constructor to initialize a player with name and starting resources
    public Player(String name, int startingGold, int startingFood) {
        this.name = name;
        this.resourceHandler = new ResourceHandler(startingGold, startingFood);
        this.wins = 0;
        this.unitCounts = new HashMap<>();
        this.unitCounts.put("Peasant", 0);
        this.unitCounts.put("Spearman", 0);
        this.unitCounts.put("Swordsman", 0);
        this.unitCounts.put("Knight", 0);

        // Assigns a color based on the name's hash code
        this.color = switch (Math.abs(name.hashCode() % 4)) {
            case 0 -> new Color(173, 216, 230); // Light blue
            case 1 -> new Color(255, 182, 193); // Light pink
            case 2 -> new Color(221, 160, 221); // Light purple
            case 3 -> new Color(216, 191, 216); // Light thistle
            default -> Color.GRAY;
        };
    }

    // Increments the count of a specific unit type
    public void incrementUnitCount(String unitType) {
        unitCounts.put(unitType, unitCounts.getOrDefault(unitType, 0) + 1);
    }

    // Decrements the count of a specific unit type
    public void decrementUnitCount(String unitType) {
        unitCounts.put(unitType, Math.max(0, unitCounts.getOrDefault(unitType, 0) - 1));
    }

    // Checks if the player can train a new unit of the specified type
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

    // Calculates the total unit space available from the player's structures
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

    // Checks if the player has enough unit space to train a new unit
    public boolean hasEnoughUnitSpace(GameBoard gameBoard) {
        int totalUnits = unitCounts.values().stream().mapToInt(Integer::intValue).sum();
        return totalUnits < getTotalUnitSpace(gameBoard);
    }

    // Gets the player's name
    public String getName() { return name; }

    // Gets the player's resource handler
    public ResourceHandler getResourceHandler() { return resourceHandler; }

    // Gets the number of wins
    public int getWins() { return wins; }

    // Increments the player's win count
    public void incrementWins() { this.wins++; }

    // Gets the player's color
    public Color getColor() { return color; }

    // Gets the map of unit counts
    public Map<String, Integer> getUnitCounts() { return unitCounts; }

    // Returns the player's name as a string representation
    @Override
    public String toString() { return name; }
}