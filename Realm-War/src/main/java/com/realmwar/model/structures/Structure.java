package com.realmwar.model.structures;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;

public abstract class Structure extends GameEntity {
    protected int durability;
    protected final int maxDurability;
    protected final int maintenanceCost;

    public Structure(Player owner, int x, int y, int maxDurability, int maintenanceCost) {
        super(owner, x, y);
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.maintenanceCost = maintenanceCost;
    }

    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public int getMaintenanceCost() { return maintenanceCost; }

    public void setDurability(int value) {
        if (value >= 0 && value <= this.maxDurability) {
            this.durability = value;
        } else if (value > this.maxDurability) {
            this.durability = this.maxDurability;
        } else {
            this.durability = 0;
        }
    }

    public void takeDamage(int amount) {
        this.durability -= amount;
        if (this.durability < 0) this.durability = 0;
    }

    public boolean isDestroyed() { return this.durability <= 0; }
}
