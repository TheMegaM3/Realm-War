// Spearman.java
// Represents a Spearman unit in the RealmWar game, a standard defensive melee unit.
// Extends the Unit class with specific attributes defined in Constants.

package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

// Class representing a Spearman unit
public class Spearman extends Unit {

    // Constructor to initialize a Spearman with owner, position, and stats from Constants
    public Spearman(Player owner, int x, int y) {
        // Calls the parent Unit constructor with predefined stats
        super(owner, x, y,
                Constants.SPEARMAN_HEALTH,
                Constants.SPEARMAN_ATTACK,
                Constants.SPEARMAN_RANGE,
                Constants.SPEARMAN_MOVE,
                Constants.SPEARMAN_GOLD_COST,
                Constants.SPEARMAN_FOOD_COST,
                Constants.SPEARMAN_MAINTENANCE_COST
        );
    }
}