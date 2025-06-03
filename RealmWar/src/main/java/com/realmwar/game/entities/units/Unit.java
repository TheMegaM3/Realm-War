package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.entities.blocks.Block;
import main.java.com.realmwar.game.entities.blocks.ForestBlock;
import main.java.com.realmwar.game.entities.blocks.VoidBlock;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.structures.Tower;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.CustomExceptions;
import main.java.com.realmwar.game.util.GameLogger;

public abstract class Unit {
    protected Player owner;
    protected int health;
    protected int maxHealth;
    protected int movementRange;
    protected int attackPower;
    protected int attackRange;
    protected int goldCost;
    protected int foodCost;
    protected int unitSpaceCost;
    protected int hierarchyLevel;
    protected int row, col;

    public Unit(Player owner, int health, int movementRange, int attackPower,
                int attackRange, int goldCost, int foodCost, int unitSpaceCost,
                int hierarchyLevel, int row, int col) {
        this.owner = owner;
        this.health = health;
        this.maxHealth = health;
        this.movementRange = movementRange;
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.goldCost = goldCost;
        this.foodCost = foodCost;
        this.unitSpaceCost = unitSpaceCost;
        this.hierarchyLevel = hierarchyLevel;
        this.row = row;
        this.col = col;
        GameLogger.log("Unit " + this.getClass().getSimpleName() + " created for " +
                owner.getName() + " at (" + row + ", " + col + ").");
    }

    public Player getOwner() { return owner; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getMovementRange() { return movementRange; }
    public int getAttackPower() { return attackPower; }
    public int getAttackRange() { return attackRange; }
    public int getGoldCost() { return goldCost; }
    public int getFoodCost() { return foodCost; }
    public int getUnitSpaceCost() { return unitSpaceCost; }
    public int getHierarchyLevel() { return hierarchyLevel; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setPosition(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }

    public void takeDamage(int amount) {
        this.health -= amount;
        GameLogger.log(this.getClass().getSimpleName() + " at (" + row + ", " + col +
                ") took " + amount + " damage. Remaining health: " + health + ".");
        if (this.health <= 0) {
            die();
        }
    }

    protected void die() {
        GameLogger.log(this.getClass().getSimpleName() + " at (" + row + ", " + col +
                ") for " + owner.getName() + " was destroyed.");
        owner.removeUnit(this);
    }

    public void move(int targetRow, int targetCol) throws CustomExceptions.InvalidActionException,
            CustomExceptions.InvalidPlacementException {
        GameManager gameManager = GameManager.getInstance();
        int distance = Math.abs(this.row - targetRow) + Math.abs(this.col - targetCol);

        if (distance > this.movementRange) {
            throw new CustomExceptions.InvalidActionException("Target is out of unit's movement range.");
        }

        Block targetBlock = gameManager.getBlockAt(targetRow, targetCol);

        if (targetBlock instanceof VoidBlock) {
            throw new CustomExceptions.InvalidPlacementException("Cannot move to a void block.");
        }

        if (targetBlock.getUnit() != null) {
            throw new CustomExceptions.InvalidPlacementException("Target block is already occupied by another unit.");
        }

        if (targetBlock.getStructure() != null) {
            if (targetBlock.getStructure().getOwner() != this.owner) {
                if (targetBlock.getStructure() instanceof Tower &&
                        ((Tower)targetBlock.getStructure()).blocksUnit(this)) {
                    throw new CustomExceptions.InvalidPlacementException("Movement blocked by enemy tower.");
                } else {
                    throw new CustomExceptions.InvalidPlacementException("Cannot move onto an enemy structure.");
                }
            }
        }

        GameLogger.log(this.getClass().getSimpleName() + " validated move from (" +
                this.row + "," + this.col + ") to (" + targetRow + "," + targetCol + ").");
    }

    public void attack(Unit target) throws CustomExceptions.InvalidActionException {
        if (this.owner == target.getOwner()) {
            throw new CustomExceptions.InvalidActionException("Cannot attack your own unit.");
        }

        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange) {
            throw new CustomExceptions.InvalidActionException("Target unit is out of attack range.");
        }

        int finalAttackPower = this.attackPower;
        Block currentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);

        if (currentBlock instanceof ForestBlock) {
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Forest advantage applied. Attack power increased to: " + finalAttackPower + ".");
        }

        GameLogger.log(this.getClass().getSimpleName() + " attacks " +
                target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }

    public void attack(Structure target) throws CustomExceptions.InvalidActionException {
        if (this.owner == target.getOwner()) {
            throw new CustomExceptions.InvalidActionException("Cannot attack your own structure.");
        }

        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange) {
            throw new CustomExceptions.InvalidActionException("Target structure is out of attack range.");
        }

        int finalAttackPower = this.attackPower;
        Block currentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);

        if (currentBlock instanceof ForestBlock) {
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Forest advantage applied. Attack power increased to: " + finalAttackPower + ".");
        }

        GameLogger.log(this.getClass().getSimpleName() + " attacks " +
                target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }

    public abstract Unit mergeWith(Unit other);
}