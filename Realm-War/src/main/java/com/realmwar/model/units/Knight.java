// Knight.java
// Represents a Knight unit in the RealmWar game, an elite and mobile cavalry unit.
// Extends the Unit class with specific attributes defined in Constants.

package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

// Class representing a Knight unit
public class Knight extends Unit {

    // Constructor to initialize a Knight with owner, position, and stats from Constants
    public Knight(Player owner, int x, int y) {
        // Calls the parent Unit constructor with predefined stats
        super(owner, x, y,
                Constants.KNIGHT_HEALTH,
                Constants.KNIGHT_ATTACK,
                Constants.KNIGHT_RANGE,
                Constants.KNIGHT_MOVE,
                Constants.KNIGHT_GOLD_COST,
                Constants.KNIGHT_FOOD_COST,
                Constants.KNIGHT_MAINTENANCE_COST
        );
    }
}