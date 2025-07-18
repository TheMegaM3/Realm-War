// Structure.java
// Abstract base class for all structures in the RealmWar game.
// Manages common attributes like durability, level, and maintenance cost, and provides damage and upgrade logic.

package com.realmwar.model.structures;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;
import com.realmwar.engine.GameBoard;

// Abstract base class for game structures
public abstract class Structure extends GameEntity {
    // Current durability of the structure
    protected int durability;
    // Maximum durability of the structure
    protected int maxDurability;
    // Maintenance cost per turn
    protected final int maintenanceCost;
    // Current level of the structure
    protected int level;
    // Maximum level the structure can reach
    protected final int maxLevel;

    // Constructor to initialize a structure with owner, position, and attributes
    public Structure(Player owner, int x, int y, int maxDurability, int maintenanceCost) {
        super(owner, x, y);
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.maintenanceCost = maintenanceCost;
        this.level = 1;
        this.maxLevel = 3;
    }

    // Gets the current durability
    public int getDurability() { return durability; }

    // Gets the maximum durability
    public int getMaxDurability() { return maxDurability; }

    // Gets the maintenance cost
    public int getMaintenanceCost() { return maintenanceCost; }

    // Gets the current level
    public int getLevel() { return level; }

    // Gets the maximum level
    public int getMaxLevel() { return maxLevel; }

    // Sets the durability, ensuring it stays within valid bounds
    public void setDurability(int value) {
        if (value >= 0 && value <= this.maxDurability) {
            this.durability = value;
        } else if (value > this.maxDurability) {
            this.durability = this.maxDurability;
        } else {
            this.durability = 0;
        }
    }

    // Applies damage to the structure, with reduced damage if adjacent to a friendly tower
    public void takeDamage(int amount, GameBoard board) {
        int finalDamage = amount;
        if (!(this instanceof Tower)) {
            if (board != null && board.isAdjacentToFriendlyTower(x, y, owner)) {
                finalDamage = (int) (amount * 0.5);
            }
        }
        this.durability -= finalDamage;
        if (this.durability < 0) {
            this.durability = 0;
        }
    }

    // Checks if the structure is destroyed
    @Override
    public boolean isDestroyed() {
        return this.durability <= 0;
    }

    // Upgrades the structure, increasing level and durability
    public void levelUp() {
        if (level < maxLevel) {
            level++;
            this.maxDurability += Constants.STRUCTURE_DURABILITY_INCREMENT_PER_LEVEL;
            this.durability += Constants.STRUCTURE_DURABILITY_INCREMENT_PER_LEVEL;
        }
    }
}