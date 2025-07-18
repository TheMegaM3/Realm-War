// Peasant.java
// Represents a Peasant unit in the RealmWar game, the basic worker/unit.
// Extends the Unit class with specific attributes defined in Constants.

package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

// Class representing a Peasant unit
public class Peasant extends Unit {

    // Constructor to initialize a Peasant with owner, position, and stats from Constants
    public Peasant(Player owner, int x, int y) {
        // Calls the parent Unit constructor with predefined stats
        super(owner, x, y,
                Constants.PEASANT_HEALTH,
                Constants.PEASANT_ATTACK,
                Constants.PEASANT_RANGE,
                Constants.PEASANT_MOVE,
                Constants.PEASANT_GOLD_COST,
                Constants.PEASANT_FOOD_COST,
                Constants.PEASANT_MAINTENANCE_COST
        );
    }
}