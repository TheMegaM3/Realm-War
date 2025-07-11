package com.realmwar.model;

/**
 * Represents a player in the game, holding their name, resources, and statistics.
 */
public class Player {

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
