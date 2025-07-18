// GameEntity.java
// Abstract base class for all objects that can exist on the game board in the RealmWar game.
// Defines common attributes like owner and position, and provides methods for damage and destruction.

package com.realmwar.model;

// Abstract base class for game entities
public abstract class GameEntity {

    // The player who owns this entity
    protected Player owner;
    // The x-coordinate on the game board
    protected int x;
    // The y-coordinate on the game board
    protected int y;

    // Constructor to initialize an entity with owner and position
    public GameEntity(Player owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;
    }

    // Gets the owner of the entity
    public Player getOwner() {
        return owner;
    }

    // Gets the x-coordinate
    public int getX() {
        return x;
    }

    // Gets the y-coordinate
    public int getY() {
        return y;
    }

    // Sets the position of the entity
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Checks if the entity is destroyed (overridden by subclasses)
    public boolean isDestroyed() {
        return false; // Default for entities that can't be destroyed
    }

    // Applies damage to the entity (overridden by subclasses)
    public void takeDamage(int damage) {
        // Default behavior is to take no damage
    }
}