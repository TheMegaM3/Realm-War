package main.java.com.realmwar.game.entities.structures;

import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.entities.blocks.EmptyBlock;
import main.java.com.realmwar.game.util.GameLogger;

public abstract class Structure {
    protected Player owner;
    protected int durability;
    protected int maxDurability;
    protected int maintenanceCost;
    protected int level;
    protected int maxLevel;
    protected int goldCost;
    protected int foodCost;
    protected int row, col;

    public Structure(Player owner, int initialDurability, int maintenanceCost, int initialLevel, int maxLevel, int goldCost, int foodCost, int row, int col) {
        this.owner = owner;
        this.durability = initialDurability;
        this.maxDurability = initialDurability;
        this.maintenanceCost = maintenanceCost;
        this.level = initialLevel;
        this.maxLevel = maxLevel;
        this.goldCost = goldCost;
        this.foodCost = foodCost;
        this.row = row;
        this.col = col;
        GameLogger.log("Structure " + this.getClass().getSimpleName() + " created for " + owner.getName() + " at (" + row + ", " + col + ")");
    }

    // Getters
    public Player getOwner() { return owner; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public int getMaintenanceCost() { return maintenanceCost; }
    public int getLevel() { return level; }
    public int getMaxLevel() { return maxLevel; }
    public int getGoldCost() { return goldCost; }
    public int getFoodCost() { return foodCost; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public void takeDamage(int amount) {
        this.durability -= amount;
        GameLogger.log(this.getClass().getSimpleName() + " at (" + row + ", " + col + ") took " + amount + " damage. Remaining durability: " + durability);
        if (this.durability <= 0) {
            destroy();
        }
    }

    public abstract void upgrade();
    public abstract int getProductionAmount();

    protected void destroy() {
        GameLogger.log(this.getClass().getSimpleName() + " at (" + row + ", " + col + ") for " + owner.getName() + " was destroyed.");
        owner.removeStructure(this);
        GameManager.getInstance().replaceBlock(row,col,new EmptyBlock(row,col));
    }
    public void setPosition(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }


}