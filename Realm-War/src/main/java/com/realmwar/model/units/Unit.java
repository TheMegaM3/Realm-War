// Unit.java
// Abstract base class for all movable units in the RealmWar game.
// Manages common attributes like health, attack, movement, and costs, and provides movement and damage logic.

package com.realmwar.model.units;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;
import com.realmwar.engine.GameTile;
import com.realmwar.engine.blocks.VoidBlock;
import com.realmwar.engine.GameBoard;
import com.realmwar.model.structures.Tower;

// Abstract base class for game units
public abstract class Unit extends GameEntity {

    // Current health of the unit (public for DatabaseManager access)
    public int health;
    // Maximum health of the unit
    protected final int maxHealth;
    // Attack power of the unit
    protected final int attackPower;
    // Attack range of the unit
    protected final int attackRange;
    // Movement range of the unit
    protected final int movementRange;
    // Gold cost to train the unit
    protected final int goldCost;
    // Food cost to train the unit
    protected final int foodCost;
    // Maintenance cost per turn
    protected final int maintenanceCost;
    // Tracks if the unit has acted this turn
    private boolean hasActedThisTurn = false;
    // Reference to the current tile the unit occupies
    private GameTile currentTile;

    // Constructor to initialize a unit with owner, position, and attributes
    public Unit(Player owner, int x, int y, int maxHealth, int attackPower, int attackRange, int movementRange, int goldCost, int foodCost, int maintenanceCost) {
        super(owner, x, y);
        this.maxHealth = maxHealth;
        this.health = maxHealth; // Units start with full health
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.movementRange = movementRange;
        this.goldCost = goldCost;
        this.foodCost = foodCost;
        this.maintenanceCost = maintenanceCost;
    }

    // Gets the current health
    public int getHealth() { return health; }

    // Gets the maximum health
    public int getMaxHealth() { return maxHealth; }

    // Gets the attack power
    public int getAttackPower() { return attackPower; }

    // Gets the attack range
    public int getAttackRange() { return attackRange; }

    // Gets the movement range
    public int getMovementRange() { return movementRange; }

    // Gets the gold cost to train
    public int getGoldCost() { return goldCost; }

    // Gets the food cost to train
    public int getFoodCost() { return foodCost; }

    // Gets the maintenance cost
    public int getMaintenanceCost() { return maintenanceCost; }

    // Checks if the unit has acted this turn
    public boolean hasActedThisTurn() { return hasActedThisTurn; }

    // Sets whether the unit has acted this turn
    public void setHasActedThisTurn(boolean value) { this.hasActedThisTurn = value; }

    // Gets the current tile
    public GameTile getCurrentTile() {
        return this.currentTile;
    }

    // Sets the current tile and updates its reference
    public void setCurrentTile(GameTile tile) {
        this.currentTile = tile;
    }

    // Applies damage to the unit
    @Override
    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    // Checks if the unit is destroyed
    @Override
    public boolean isDestroyed() {
        return this.health <= 0;
    }

    // Checks if the unit can move to a target tile
    public boolean canMoveTo(GameTile targetTile, GameBoard gameBoard) {
        if (currentTile == null || targetTile == null) return false;

        int dx = Math.abs(currentTile.getX() - targetTile.getX());
        int dy = Math.abs(currentTile.getY() - targetTile.getY());
        int distance = dx + dy;

        boolean inRange = distance <= movementRange;
        boolean notVoid = !(targetTile.getBlock() instanceof VoidBlock);
        boolean notOccupied = targetTile.getEntity() == null;

        boolean notBlockedByTower = true;
        if (inRange && notVoid && notOccupied) {
            int[] dxOffsets = {-1, 1, 0, 0, -1, -1, 1, 1};
            int[] dyOffsets = {0, 0, -1, 1, -1, 1, -1, 1};
            for (int i = 0; i < 8; i++) {
                GameTile adjacentTile = gameBoard.getTile(targetTile.getX() + dxOffsets[i], targetTile.getY() + dyOffsets[i]);
                if (adjacentTile != null && adjacentTile.getEntity() instanceof Tower tower && tower.getOwner() != this.getOwner()) {
                    if (tower.blocksUnit(this)) {
                        notBlockedByTower = false;
                        break;
                    }
                }
            }
        }

        return inRange && notVoid && notOccupied && notBlockedByTower;
    }

    // Moves the unit to a target tile if allowed
    public boolean moveTo(GameTile targetTile, GameBoard gameBoard) {
        if (canMoveTo(targetTile, gameBoard)) {
            if (currentTile != null) {
                currentTile.setEntity(null);
            }
            targetTile.setEntity(this);
            setPosition(targetTile.getX(), targetTile.getY());

            // Claims territory for the unit's owner
            targetTile.setTerritoryOwner(this.getOwner());

            return true;
        }
        return false;
    }

    // Gets the unit's level based on its type
    public int getUnitLevel() {
        if (this instanceof Peasant) return 1;
        if (this instanceof Spearman) return 2;
        if (this instanceof Swordsman) return 3;
        if (this instanceof Knight) return 4;
        return 0; // Default case
    }
}