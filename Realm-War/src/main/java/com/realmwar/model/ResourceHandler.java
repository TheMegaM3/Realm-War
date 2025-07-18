// ResourceHandler.java
// Manages a player's resources (gold and food) in the RealmWar game.
// Enforces rules for adding and spending resources, throwing exceptions for invalid operations.

package com.realmwar.model;

import com.realmwar.util.CustomExceptions.GameRuleException;

// Class for managing player resources
public class ResourceHandler {
    // Current amount of gold
    private int gold;
    // Current amount of food
    private int food;

    // Constructor to initialize resources with starting amounts
    public ResourceHandler(int initialGold, int initialFood) {
        this.gold = initialGold;
        this.food = initialFood;
    }

    // Gets the current gold amount
    public int getGold() {
        return gold;
    }

    // Gets the current food amount
    public int getFood() {
        return food;
    }

    // Adds specified amounts of gold and food to the player's resources
    public void addResources(int goldAmount, int foodAmount) {
        this.gold += goldAmount;
        this.food += foodAmount;
    }

    // Spends resources, checking for sufficient amounts before deducting
    public void spendResources(int goldCost, int foodCost) throws GameRuleException {
        if (this.gold < goldCost || this.food < foodCost) {
            throw new GameRuleException("Insufficient Resources!");
        }
        this.gold -= goldCost;
        this.food -= foodCost;
    }
}