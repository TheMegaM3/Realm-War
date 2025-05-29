package main.java.com.realmwar.game.entities.structures;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;

public class Barrack extends Structure {
    private int unitSpaceProvided;

    public Barrack(Player owner, int row, int col) {
        super(owner, Constants.BARRACK_DURABILITY, Constants.BARRACK_MAINTENANCE, 1, Constants.BARRACK_MAX_LEVEL, Constants.BARRACK_GOLD_COST, Constants.BARRACK_FOOD_COST, row, col);
        this.unitSpaceProvided = Constants.BARRACK_INITIAL_UNIT_SPACE;
        GameLogger.log("Barrack created for " + owner.getName() + " at (" + row + ", " + col + "). Provides " + unitSpaceProvided + " unit space.");
    }

    @Override
    public void upgrade() {
        if (this.level < this.maxLevel) {
            this.level++;
            this.durability += Constants.BARRACK_DURABILITY_PER_LEVEL;
            this.maxDurability += Constants.BARRACK_DURABILITY_PER_LEVEL;
            int oldSpace = this.unitSpaceProvided;
            this.unitSpaceProvided += Constants.BARRACK_UNIT_SPACE_PER_LEVEL;
            owner.updateMaxUnitSpace(this.unitSpaceProvided - oldSpace);
            GameLogger.log("Barrack upgraded to level " + this.level + ". New unit space: " + unitSpaceProvided + ".");
        } else {
            GameLogger.logWarning("Barrack is already at max level.");
        }
    }

    @Override
    public int getProductionAmount() {
        return 0;
    }

    public int getUnitSpaceProvided() {
        return unitSpaceProvided;
    }
}

