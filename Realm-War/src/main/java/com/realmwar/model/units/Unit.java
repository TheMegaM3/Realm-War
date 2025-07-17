package com.realmwar.model.units;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;
import com.realmwar.engine.GameTile;
import com.realmwar.engine.blocks.VoidBlock;
import com.realmwar.engine.GameBoard;
import com.realmwar.model.structures.Tower;

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

    private GameTile currentTile; // track current tile without needing GameManager

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

    public GameTile getCurrentTile() {
        return this.currentTile;
    }

    public void setCurrentTile(GameTile tile) {
        this.currentTile = tile;
    }

    @Override
    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    @Override
    public boolean isDestroyed() {
        return this.health <= 0;
    }

    // --- Movement Logic ---

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

    public boolean moveTo(GameTile targetTile, GameBoard gameBoard) {
        if (canMoveTo(targetTile, gameBoard)) {
            if (currentTile != null) {
                currentTile.setEntity(null);
            }
            targetTile.setEntity(this);
            setPosition(targetTile.getX(), targetTile.getY());

            // 🟢 ثبت قلمرو جدید برای بازیکن صاحب یونیت
            targetTile.setTerritoryOwner(this.getOwner());

            return true;
        }
        return false;
    }

    public int getUnitLevel() {
        if (this instanceof Peasant) return 1;
        if (this instanceof Spearman) return 2;
        if (this instanceof Swordsman) return 3;
        if (this instanceof Knight) return 4;
        return 0; // Default case
    }
}
