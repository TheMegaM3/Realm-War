package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;

/**
 * Represents a Peasant unit, the basic worker/unit.
 */
public class Peasant extends Unit {
    public Peasant(Player owner, int x, int y) {
        // Calls the parent constructor with all stats from the Constants class.
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