package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

/**
 * Represents a Spearman unit, a standard defensive melee unit.
 */
public class Spearman extends Unit {

    /**
     * Constructs a new Spearman.
     * @param owner The player who owns this unit.
     * @param x     The initial x-coordinate.
     * @param y     The initial y-coordinate.
     */
    public Spearman(Player owner, int x, int y) {
        // Calls the parent Unit constructor with all stats defined in the Constants class.
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
