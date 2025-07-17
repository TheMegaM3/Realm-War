package com.realmwar.model.structures;

import com.realmwar.engine.GameBoard;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;

public abstract class Structure extends GameEntity {
    protected int durability;
    protected int maxDurability;
    protected final int maintenanceCost;
    protected int level;
    protected final int maxLevel;

    public Structure(Player owner, int x, int y, int maxDurability, int maintenanceCost) {
        super(owner, x, y);
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.maintenanceCost = maintenanceCost;
        this.level = 1;
        this.maxLevel = 3;
    }

    // MODIFIED: Added @Override annotation to fix compile error.
    @Override
    public void takeDamage(int amount, GameBoard board) {
        int finalDamage = amount;
        // Towers don't protect themselves, but they protect other structures.
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

    @Override
    public boolean isDestroyed() {
        return this.durability <= 0;
    }

    // Getters and other methods
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public int getMaintenanceCost() { return maintenanceCost; }
    public int getLevel() { return level; }
    public int getMaxLevel() { return maxLevel; }

    public void setDurability(int value) {
        if (value >= 0 && value <= this.maxDurability) {
            this.durability = value;
        } else if (value > this.maxDurability) {
            this.durability = this.maxDurability;
        } else {
            this.durability = 0;
        }
    }

    public void levelUp() {
        if (level < maxLevel) {
            level++;
            this.maxDurability += Constants.STRUCTURE_DURABILITY_INCREMENT_PER_LEVEL;
            this.durability += Constants.STRUCTURE_DURABILITY_INCREMENT_PER_LEVEL;
        }
    }
}
