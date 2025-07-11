package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

/**
 * Represents a Knight unit, an elite and mobile cavalry unit.
 */
public class Knight extends Unit {

    /**
     * Constructs a new Knight.
     * @param owner The player who owns this unit.
     * @param x     The initial x-coordinate.
     * @param y     The initial y-coordinate.
     */
    public Knight(Player owner, int x, int y) {
        // Calls the parent Unit constructor with all stats defined in the Constants class.
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
