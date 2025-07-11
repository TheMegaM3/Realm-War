package com.realmwar.model.structures;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;

/**
 * The abstract base class for all non-movable building entities in the game.
 * All structures have durability, maintenance costs, and can be leveled up.
 */
public abstract class Structure extends GameEntity {

    protected int durability;
    protected final int maxDurability;
    protected final int maintenanceCost;
    protected int level;
    protected final int maxLevel;

    public Structure(Player owner, int x, int y, int maxDurability, int maintenanceCost) {
        super(owner, x, y);
        this.maxDurability = maxDurability;
        this.durability = maxDurability; // Structures start with full durability.
        this.maintenanceCost = maintenanceCost;
        this.level = 1;
        this.maxLevel = 3; // All structures can be upgraded up to level 3.
    }

    // --- Getters ---

    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public int getMaintenanceCost() { return maintenanceCost; }
    public int getLevel() { return level; }
    public int getMaxLevel() { return maxLevel; }

    /**
     * Sets the durability, ensuring it stays within the valid range [0, maxDurability].
     * @param value The new durability value.
     */
    public void setDurability(int value) {
        if (value >= 0 && value <= this.maxDurability) {
            this.durability = value;
        } else if (value > this.maxDurability) {
            this.durability = this.maxDurability;
        } else {
            this.durability = 0;
        }
    }

    /**
     * Reduces the structure's durability by a given amount.
     * @param amount The amount of damage to take.
     */
    @Override
    public void takeDamage(int amount) {
        this.durability -= amount;
        if (this.durability < 0) {
            this.durability = 0;
        }
    }

    /**
     * Checks if the structure has been destroyed.
     * @return true if durability is 0 or less.
     */
    @Override
    public boolean isDestroyed() {
        return this.durability <= 0;
    }

    /**
     * Increases the structure's level if it's not already at max level.
     */
    public void levelUp() {
        if (level < maxLevel) {
            level++;
        }
    }
}
