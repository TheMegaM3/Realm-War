package main.java.com.realmwar.game.entities.structures;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;

public class TownHall extends Structure {

    public TownHall(Player owner, int row, int col) {
        super(owner, Constants.TOWNHALL_DURABILITY, 0, 1, 1, Constants.TOWNHALL_GOLD_COST, Constants.TOWNHALL_FOOD_COST, row, col);
        GameLogger.log("TownHall created for " + owner.getName() + " at (" + row + ", " + col + ").");
    }

    @Override
    public void upgrade() {
        GameLogger.logWarning("TownHall cannot be upgraded (Max Level: 1).");
    }

    @Override
    public int getProductionAmount() {
        return 0;
    }
}

