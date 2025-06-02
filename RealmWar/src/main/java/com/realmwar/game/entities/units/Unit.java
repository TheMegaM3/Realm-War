package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.entities.structures.Structure;

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
    protected int row;
    protected int col;

    public Unit(Player owner, int health, int maxHealth, int movementRange, int attackPower, int attackRange , int goldCost, int foodCost , int unitSpaceCost , int row, int col) {

        this.owner = owner;
        this.health = health;
        this.maxHealth = maxHealth;
        this.movementRange = movementRange;
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.goldCost = goldCost;
        this.foodCost = foodCost;
        this.unitSpaceCost = unitSpaceCost;
        this.row = row;
        this.col = this.col;

        GameLogger.log("Unit " + this.getClass().getSimpleName() + " created for " + owner.getName() + " at (" + row + ", " + this.col + " )");
    }

    public Player getOwner() {
        return owner;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getMovementRange() {
        return movementRange;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public int getUnitSpaceCost() {
        return unitSpaceCost;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void takeDamage(int amount) {
        this.health -= amount;
        GameLogger.log(this.getClass().getSimpleName() + " at (" + row + ", " + col + "took  " + amount + " damage. Remaining health: " + health);
        if (this.health <= 0){
            die();
        }
    }

    public void die(){
        GameLogger.log(this.getClass().getSimpleName() + " at(" + row + ", " + col + ") for " + owner.getName() + " was destroyed.");
        owner.removeUnit(this);
    }

    public abstract void move(int targetRow, int targetCol);
    public abstract void attck(Unit target);

    public abstract void attack(Unit target);

    public abstract void attack(Structure target);

    protected void setPosition(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }
}
