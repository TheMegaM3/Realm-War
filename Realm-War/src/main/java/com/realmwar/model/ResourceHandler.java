package com.realmwar.model;

import com.realmwar.util.CustomExceptions.GameRuleException;

/**
 * Manages a player's resources (gold and food) and enforces spending rules.
 */
public class ResourceHandler {
    private int gold;
    private int food;

    public ResourceHandler(int initialGold, int initialFood) {
        this.gold = initialGold;
        this.food = initialFood;
    }

    // --- Getters ---

    public int getGold() {
        return gold;
    }

    public int getFood() {
        return food;
    }

    /**
     * Adds a specified amount of gold and food to the player's resources.
     * @param goldAmount The amount of gold to add.
     * @param foodAmount The amount of food to add.
     */
    public void addResources(int goldAmount, int foodAmount) {
        this.gold += goldAmount;
        this.food += foodAmount;
    }

    /**
     * Spends resources, checking if the player has enough before deducting.
     * @param goldCost The amount of gold to spend.
     * @param foodCost The amount of food to spend.
     * @throws GameRuleException if the player has insufficient resources.
     */
    public void spendResources(int goldCost, int foodCost) throws GameRuleException {
        if (this.gold < goldCost || this.food < foodCost) {
            throw new GameRuleException("Insufficient Resources!");
        }
        this.gold -= goldCost;
        this.food -= foodCost;
    }
}
