package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;

public class Knight extends Unit {
    public Knight(Player owner, int row, int col) {
        super(owner,
                Constants.KNIGHT_HEALTH,
                Constants.KNIGHT_MOVEMENT_RANGE,
                Constants.KNIGHT_ATTACK_POWER,
                Constants.KNIGHT_ATTACK_RANGE,
                Constants.KNIGHT_GOLD_COST,
                Constants.KNIGHT_FOOD_COST,
                Constants.KNIGHT_UNIT_SPACE_COST,
                Constants.KNIGHT_HIERARCHY_LEVEL,
                row, col);
        GameLogger.log("Knight created for " + owner.getName() + ".");
    }

    @Override
    public Unit mergeWith(Unit other) {
        GameLogger.logWarning("Knight cannot be merged further.");
        return null;
    }
}