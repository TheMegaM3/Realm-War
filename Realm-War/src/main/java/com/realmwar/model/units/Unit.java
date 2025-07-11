package com.realmwar.model.units;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;

/**
 * Represents the abstract base class for all movable units in the game.
 * All units have health, attack stats, costs, and can perform actions.
 */
public abstract class Unit extends GameEntity {

    // Public for easy access from the DatabaseManager, though getters/setters are often preferred.
    public int health;
    protected final int maxHealth;
    protected final int attackPower;
    protected final int attackRange;
    protected final int movementRange;
    //  Costs are final fields for better design and efficiency.
    protected final int goldCost;
    protected final int foodCost;
    protected final int maintenanceCost;

    private boolean hasActedThisTurn = false;

    public Unit(Player owner, int x, int y, int maxHealth, int attackPower, int attackRange, int movementRange, int goldCost, int foodCost, int maintenanceCost) {
        super(owner, x, y);
        this.maxHealth = maxHealth;
        this.health = maxHealth; // Units start with full health.
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.movementRange = movementRange;
        this.goldCost = goldCost;
        this.foodCost = foodCost;
        this.maintenanceCost = maintenanceCost;
    }

    // --- Getters and Setters ---

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getAttackPower() { return attackPower; }
    public int getAttackRange() { return attackRange; }
    public int getMovementRange() { return movementRange; }
    public int getGoldCost() { return goldCost; }
    public int getFoodCost() { return foodCost; }
    public int getMaintenanceCost() { return maintenanceCost; }

    public boolean hasActedThisTurn() { return hasActedThisTurn; }
    public void setHasActedThisTurn(boolean value) { this.hasActedThisTurn = value; }

    @Override
    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    @Override
    public boolean isDestroyed() {
        return this.health <= 0;
    }

}
