package com.realmwar.model.units;

import com.realmwar.data.GameLogger;
import com.realmwar.engine.GameBoard;
import com.realmwar.engine.GameTile;
import com.realmwar.engine.blocks.VoidBlock;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;

public abstract class Unit extends GameEntity {

    public int health;
    protected final int maxHealth;
    protected final int attackPower;
    protected final int attackRange;
    protected final int movementRange;
    protected final int goldCost;
    protected final int foodCost;
    protected final int maintenanceCost;
    private boolean hasActedThisTurn = false;
    private GameTile currentTile;

    public Unit(Player owner, int x, int y, int maxHealth, int attackPower, int attackRange, int movementRange, int goldCost, int foodCost, int maintenanceCost) {
        super(owner, x, y);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.movementRange = movementRange;
        this.goldCost = goldCost;
        this.foodCost = foodCost;
        this.maintenanceCost = maintenanceCost;
    }

    // MODIFIED: takeDamage now implements the 50% damage reduction from towers.
    @Override
    public void takeDamage(int amount, GameBoard board) {
        int finalDamage = amount;
        // Apply 50% damage reduction if adjacent to a friendly tower
        if (board != null && board.isAdjacentToFriendlyTower(this.x, this.y, this.owner)) {
            finalDamage = (int) (amount * 0.5);
            GameLogger.log(this.getClass().getSimpleName() + " at (" + this.x + "," + this.y + ") is protected by a tower! Damage reduced to " + finalDamage);
        }
        this.health -= finalDamage;
        if (this.health < 0) this.health = 0;
    }

    @Override
    public boolean isDestroyed() {
        return this.health <= 0;
    }

    // ... (The rest of the Unit class remains the same) ...

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
    public GameTile getCurrentTile() { return this.currentTile; }
    public void setCurrentTile(GameTile tile) { this.currentTile = tile; }

    public boolean canMoveTo(GameTile targetTile, GameBoard gameBoard) {
        if (currentTile == null || targetTile == null || hasActedThisTurn) return false;
        int dx = Math.abs(currentTile.getX() - targetTile.getX());
        int dy = Math.abs(currentTile.getY() - targetTile.getY());
        int distance = dx + dy;
        if (distance == 0 || distance > movementRange) {
            return false;
        }
        if (targetTile.getBlock() instanceof VoidBlock || targetTile.getEntity() != null) {
            return false;
        }
        boolean isFriendlyTerritory = targetTile.getOwner() == this.owner;
        boolean isAdjacentNeutral = targetTile.getOwner() == null && gameBoard.isAdjacentToTerritory(targetTile.getX(), targetTile.getY(), this.owner);
        return isFriendlyTerritory || isAdjacentNeutral;
    }
    public boolean moveTo(GameTile targetTile, GameBoard gameBoard) {
        if (canMoveTo(targetTile, gameBoard)) {
            if (currentTile != null) {
                currentTile.setEntity(null);
            }
            targetTile.setEntity(this);
            setPosition(targetTile.getX(), targetTile.getY());
            this.currentTile = targetTile;
            return true;
        }
        return false;
    }
    public int getUnitLevel() {
        if (this instanceof Peasant) return 1;
        if (this instanceof Spearman) return 2;
        if (this instanceof Swordsman) return 3;
        if (this instanceof Knight) return 4;
        return 0;
    }
}
