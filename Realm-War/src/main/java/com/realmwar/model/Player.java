package com.realmwar.model;

import java.awt.*;

/**
 * Represents a player in the game, holding their name, resources, and statistics.
 */
public class Player {

    private Color color;

    // The name of the player. 'final' means it cannot be changed after creation.
    private final String name;
    // A reference to the handler that manages this player's gold and food.
    private final ResourceHandler resourceHandler;
    // A field to track the number of wins for this player (for potential future use).
    private int wins;

    public Player(String name, int startingGold, int startingFood) {
        this.name = name;
        this.resourceHandler = new ResourceHandler(startingGold, startingFood);
        this.wins = 0; // Wins start at zero.

        // Assign a default color based on name hash (you can change this logic to ID-based if needed)
        this.color = switch (Math.abs(name.hashCode() % 4)) {
            case 0 -> new Color(173, 216, 230); // Light Blue
            case 1 -> new Color(255, 182, 193); // Light Pink
            case 2 -> new Color(221, 160, 221); // Plum
            case 3 -> new Color(216, 191, 216); // Light Purple
            default -> Color.GRAY;
        };
    }

    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public int getWins() {
        return wins;
    }

    public void incrementWins() {
        this.wins++;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Provides a string representation of the Player object, which is simply their name.
     * Useful for UI components like lists or labels.
     * @return The name of the player.
     */
    @Override
    public String toString() {
        return name;
    }
}
