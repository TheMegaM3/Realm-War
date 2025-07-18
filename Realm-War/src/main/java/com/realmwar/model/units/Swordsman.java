// Swordsman.java
// Represents a Swordsman unit in the RealmWar game, a strong offensive melee unit.
// Extends the Unit class with specific attributes defined in Constants.

package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

// Class representing a Swordsman unit
public class Swordsman extends Unit {

    // Constructor to initialize a Swordsman with owner, position, and stats from Constants
    public Swordsman(Player owner, int x, int y) {
        // Calls the parent Unit constructor with predefined stats
        super(owner, x, y,
                Constants.SWORDSMAN_HEALTH,
                Constants.SWORDSMAN_ATTACK,
                Constants.SWORDSMAN_RANGE,
                Constants.SWORDSMAN_MOVE,
                Constants.SWORDSMAN_GOLD_COST,
                Constants.SWORDSMAN_FOOD_COST,
                Constants.SWORDSMAN_MAINTENANCE_COST
        );
    }
}