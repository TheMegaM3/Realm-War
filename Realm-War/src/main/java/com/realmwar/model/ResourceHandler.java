package com.realmwar.model;

import com.realmwar.util.Constants;
import com.realmwar.util.CustomExceptions.*;
import com.realmwar.util.CustomExceptions;

/**
 * Manages a player's resources (gold and food).
 */
public class ResourceHandler {
    private int gold;
    private int food;

    public ResourceHandler(int initialGold, int initialFood) {
        this.gold = initialGold;
        this.food = initialFood;
    }

    public int getGold() { return gold; }
    public int getFood() { return food; }

    public void addResources(int goldAmount, int foodAmount) {
        this.gold += goldAmount;
        this.food += foodAmount;
    }

    public void spendResources(int goldCost, int foodCost) throws GameRuleException {
        if (this.gold < goldCost || this.food < foodCost) {
            throw new GameRuleException("Insufficient Resources!");
        }
        this.gold -= goldCost;
        this.food -= foodCost;
    }
}
