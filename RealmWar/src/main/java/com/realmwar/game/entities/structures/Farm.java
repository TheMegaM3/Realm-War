package main.java.com.realmwar.game.entities.structures;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;


public class Farm extends Structure {
    private int productionAmount;


    public Farm(Player owner, int row, int col) {
        super(owner, Constants.FARM_DURABILITY, Constants.FARM_MAINTENANCE, 1, Constants.FARM_MAX_LEVEL, Constants.FARM_GOLD_COST, Constants.FARM_FOOD_COST, row, col);
        this.productionAmount = Constants.FARM_INITIAL_PRODUCTION;
        GameLogger.log("Farm created for " + owner.getName() + " at (" + row + ", " + col + "). Produces " + productionAmount + " food.");
    }

    @Override
    public void upgrade() {
        if (this.level < this.maxLevel) {
            this.level++;
            this.durability += Constants.FARM_DURABILITY_PER_LEVEL;
            this.maxDurability += Constants.FARM_DURABILITY_PER_LEVEL;
            this.productionAmount += Constants.FARM_PRODUCTION_PER_LEVEL;
            GameLogger.log("Farm upgraded to level " + this.level + ". New food production: " + productionAmount + ".");
        } else {
            GameLogger.logWarning("Farm is already at max level.");
        }
    }

    @Override
    public int getProductionAmount() {
        return productionAmount;
    }
}