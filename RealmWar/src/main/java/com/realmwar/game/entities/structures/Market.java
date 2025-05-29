package main.java.com.realmwar.game.entities.structures;



import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;

public class Market extends Structure {
    private int productionAmount;

    public Market(Player owner, int row, int col) {
        super(owner, Constants.MARKET_DURABILITY, Constants.MARKET_MAINTENANCE, 1, Constants.MARKET_MAX_LEVEL, Constants.MARKET_GOLD_COST, Constants.MARKET_FOOD_COST, row, col);
        this.productionAmount = Constants.MARKET_INITIAL_PRODUCTION;

        GameLogger.log("Market created for " + owner.getName() + " at (" + row + ", " + col + "). Produces " + productionAmount + " gold.");
    }

    @Override
    public void upgrade() {
        if (this.level < this.maxLevel) {
            this.level++;
            this.durability += Constants.MARKET_DURABILITY_PER_LEVEL;
            this.maxDurability += Constants.MARKET_DURABILITY_PER_LEVEL;
            this.productionAmount += Constants.MARKET_PRODUCTION_PER_LEVEL;
            GameLogger.log("Market upgraded to level " + this.level + ". New gold production: " + productionAmount + ".");
        } else {
            GameLogger.logWarning("Market is already at max level.");
        }
    }

    @Override
    public int getProductionAmount() {
        return productionAmount;
    }
}

