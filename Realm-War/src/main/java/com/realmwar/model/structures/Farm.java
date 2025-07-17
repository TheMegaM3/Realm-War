package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

public class Farm extends Structure {
    public Farm(Player owner, int x, int y) {
        super(owner, x, y, Constants.FARM_DURABILITY, Constants.FARM_MAINTENANCE);
    }

    /**
     * Calculates food production based on the structure's level.
     * @return The amount of food produced per tick.
     */
    // MODIFIED: Added missing method
    public int getFoodProduction() {
        return Constants.FARM_FOOD_PER_TICK + (level - 1) * Constants.FARM_FOOD_INCREMENT_PER_LEVEL;
    }
}
