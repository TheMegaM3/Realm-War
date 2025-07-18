// Farm.java
// Represents a Farm structure in the RealmWar game, which produces food resources.
// Extends the Structure class to include food production logic based on level.

package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

// Class representing a Farm structure
public class Farm extends Structure {
    // Constructor to initialize a Farm with owner, position, and default attributes
    public Farm(Player owner, int x, int y) {
        super(owner, x, y, Constants.FARM_DURABILITY, Constants.FARM_MAINTENANCE);
    }

    // Calculates food production based on the farm's level
    public int getFoodProduction() {
        return Constants.FARM_FOOD_PER_TICK + (level - 1) * Constants.FARM_FOOD_INCREMENT_PER_LEVEL;
    }
}