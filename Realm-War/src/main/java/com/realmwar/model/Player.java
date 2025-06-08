package com.realmwar.model;

/**
 * Represents a player, holding their name and resource handler.
 */
public class Player {
    private final String name;
    private final ResourceHandler resourceHandler;

    public Player(String name, int startingGold, int startingFood) {
        this.name = name;
        this.resourceHandler = new ResourceHandler(startingGold, startingFood);
    }

    public String getName() {
        return name;
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    @Override
    public String toString() {
        return name;
    }

}
