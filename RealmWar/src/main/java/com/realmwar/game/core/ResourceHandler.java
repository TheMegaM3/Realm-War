package main.java.com.realmwar.game.core;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.entities.structures.Farm;
import main.java.com.realmwar.game.entities.structures.Market;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.util.CustomExceptions;
import main.java.com.realmwar.game.util.GameLogger;

public class ResourceHandler {
    private int gold;
    private int food;
    private Player owner;

    public ResourceHandler(int initialGold, int initialFood) {
        this.gold = initialGold;
        this.food = initialFood;
        GameLogger.log("ResourceHandler initialized with Gold: " + gold + ", Food: " + food + ".");
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    // Getters
    public int getGold() {
        return gold;
    }

    public int getFood() {
        return food;
    }

    public void addGold(int amount) {
        this.gold += amount;
        GameLogger.log("Gold added: " + amount + ". Total: " + gold + ".");
    }

    public void addFood(int amount) {
        this.food += amount;
        GameLogger.log("Food added: " + amount + ". Total: " + food + ".");
    }

    public void deductResources(int goldCost, int foodCost) throws CustomExceptions.InsufficientResourcesException {
        if (gold < goldCost || food < foodCost) {
            throw new CustomExceptions.InsufficientResourcesException("Not enough resources. Required: Gold " + goldCost + ", Food " + foodCost + ". Available: Gold " + gold + ", Food " + food + ".");
        }
        this.gold -= goldCost;
        this.food -= foodCost;
        GameLogger.log("Resources deducted: Gold " + goldCost + ", Food " + foodCost + ". Remaining: Gold " + gold + ", Food " + food + ".");
    }

    public boolean canAfford(int goldCost, int foodCost) {
        return this.gold >= goldCost && this.food >= foodCost;
    }

    public void generateResources() {
        int producedGold = 0;
        int producedFood = 0;
        int maintenanceCost = 0;

        if (owner != null) {
            for (Structure structure : owner.getStructures()) {
                if (structure instanceof Market) {
                    producedGold += ((Market) structure).getProductionAmount();
                } else if (structure instanceof Farm) {
                    producedFood += ((Farm) structure).getProductionAmount();
                }
                maintenanceCost += structure.getMaintenanceCost();
            }
        }

        addGold(producedGold);
        addFood(producedFood);

        try {
            deductResources(maintenanceCost, 0);
        } catch (CustomExceptions.InsufficientResourcesException e) {
            GameLogger.logError("Player " + owner.getName() + " cannot pay maintenance costs. Some structures might be damaged or destroyed: " + e.getMessage());
            int goldDeficit = maintenanceCost - this.gold;
            if (goldDeficit > 0) {
                GameLogger.logWarning("Gold deficit for maintenance: " + goldDeficit + ". Structures will take damage.");

                for (Structure structure : owner.getStructures()) {
                    if (structure.getMaintenanceCost() > 0) {
                        structure.takeDamage(goldDeficit / owner.getStructures().size()); // Proportional damage
                    }
                }
            }
        }
        GameLogger.log("Resources generated for " + (owner != null ? owner.getName() : "N/A") + ". Gold: " + producedGold + ", Food: " + producedFood + ". Maintenance: " + maintenanceCost + ".");
    }
}