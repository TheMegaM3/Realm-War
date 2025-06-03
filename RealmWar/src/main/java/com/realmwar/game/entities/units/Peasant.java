package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.entities.units.Spearman;

public class Peasant extends Unit {
    public Peasant(Player owner, int row, int col) {
        super(owner, Constants.PEASANT_HEALTH,
                Constants.PEASANT_MOVEMENT_RANGE,
                Constants.PEASANT_ATTACK_POWER,
                Constants.PEASANT_ATTACK_RANGE,
                Constants.PEASANT_GOLD_COST,
                Constants.PEASANT_FOOD_COST,
                Constants.PEASANT_UNIT_SPACE_COST,
                Constants.PEASANT_HIERARCHY_LEVEL,
                row, col);
        GameLogger.log("Peasant created for " + owner.getName() + ".");
    }

    @Override
    public Unit mergeWith(Unit other) {
        if (other instanceof Peasant && this.owner == other.getOwner()) {
            GameLogger.log("Two Peasants (owned by " + this.owner.getName() +
                    ") merged into a Spearman.");
            this.owner.removeUnit(this);
            this.owner.removeUnit(other);
            return new Spearman(this.owner, this.row, this.col);
        }
        GameLogger.logWarning("Cannot merge Peasant with " +
                (other != null ? other.getClass().getSimpleName() : "null") +
                ". Must be another Peasant owned by the same player.");
        return null;
    }
}