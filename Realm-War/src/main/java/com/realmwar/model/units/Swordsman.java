package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

/**
 * Represents a Swordsman unit, a strong offensive melee unit.
 */
public class Swordsman extends Unit {

    /**
     * Constructs a new Swordsman.
     * @param owner The player who owns this unit.
     * @param x     The initial x-coordinate.
     * @param y     The initial y-coordinate.
     */
    public Swordsman(Player owner, int x, int y) {
        // Calls the parent Unit constructor with all stats defined in the Constants class.
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
