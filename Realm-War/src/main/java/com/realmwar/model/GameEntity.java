package com.realmwar.model;

/**
 * Represents the abstract base class for any object that can exist on the game board.
 * All entities have an owner and a position (x, y).
 */
public abstract class GameEntity {

    // The player who owns this entity. 'protected' means it's accessible by subclasses.
    protected Player owner;
    // The coordinates of the entity on the game board.
    protected int x, y;

    public GameEntity(Player owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;
    }

    // --- Getters and Setters ---

    public Player getOwner() {
        return owner;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Checks if the entity is destroyed (e.g., health is zero).
     * Subclasses must override this to provide specific logic.
     * @return true if the entity is destroyed, false otherwise.
     */
    public boolean isDestroyed() {
        return false; // Default for entities that can't be destroyed (e.g., terrain).
    }

    /**
     * Applies a certain amount of damage to the entity.
     * Subclasses must override this to handle damage.
     * @param damage The amount of damage to take.
     */
    public void takeDamage(int damage) {
        // Default behavior is to take no damage.
    }
}
