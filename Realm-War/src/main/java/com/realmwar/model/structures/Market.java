package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

public class Market extends Structure {
    public Market(Player owner, int x, int y) {
        super(owner, x, y, Constants.MARKET_DURABILITY, Constants.MARKET_MAINTENANCE);
    }

    /**
     * Calculates gold production based on the structure's level.
     * @return The amount of gold produced per tick.
     */
    // MODIFIED: Added missing method
    public int getGoldProduction() {
        return Constants.MARKET_GOLD_PER_TICK + (level - 1) * Constants.MARKET_GOLD_INCREMENT_PER_LEVEL;
    }
}
