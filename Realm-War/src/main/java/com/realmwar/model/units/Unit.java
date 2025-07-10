package com.realmwar.model.units;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.util.Constants;

public abstract class Unit extends GameEntity {
    public int health;
    public final int maxHealth;
    public final int attackPower;
    public final int attackRange;
    public final int movementRange;
    private boolean hasActedThisTurn=false;
    public Unit(Player o, int x, int y, int h, int a, int ar, int mr) {
        super(o,x,y);
        this.maxHealth=h;
        this.health=h;
        this.attackPower=a;
        this.attackRange=ar;
        this.movementRange=mr; }
    public int getHealth() {
        return health; }

    public int getMaxHealth() {
        return maxHealth; }

    public int getAttackPower() {
        return attackPower; }

    public int getAttackRange() {
        return attackRange; }

    public int getMovementRange() {
        return movementRange; }

    public boolean hasActedThisTurn() {
        return hasActedThisTurn; }

    public void setHasActedThisTurn(boolean value) {
        this.hasActedThisTurn=value; }

    @Override public void takeDamage(int amount) {
        this.health-=amount; if(this.health<0) this.health=0; }

    @Override public boolean isDestroyed() {
        return this.health<=0; }

    public int getGoldCost() {
        return switch (this.getClass().getSimpleName()) {
            case "Peasant" -> Constants.PEASANT_GOLD_COST;
            case "Spearman" -> Constants.SPEARMAN_GOLD_COST;
            case "Swordsman" -> Constants.SWORDSMAN_GOLD_COST;
            case "Knight" -> Constants.KNIGHT_GOLD_COST;
            default -> 0;
        };
    }

    public int getFoodCost() {
        return switch (this.getClass().getSimpleName()) {
            case "Peasant" -> Constants.PEASANT_FOOD_COST;
            case "Spearman" -> Constants.SPEARMAN_FOOD_COST;
            case "Swordsman" -> Constants.SWORDSMAN_FOOD_COST;
            case "Knight" -> Constants.KNIGHT_FOOD_COST;
            default -> 0;
        };
    }

    public int getMaintenanceCost() {
        return switch (this.getClass().getSimpleName()) {
            case "Spearman" -> Constants.SPEARMAN_MAINTENANCE_COST;
            case "Swordsman" -> Constants.SWORDSMAN_MAINTENANCE_COST;
            case "Knight" -> Constants.KNIGHT_MAINTENANCE_COST;
            default -> 0; // Peasant و دیگر موارد هزینه‌ای ندارند
        };
    }

}
