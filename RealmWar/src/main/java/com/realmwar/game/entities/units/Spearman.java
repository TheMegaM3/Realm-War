package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.entities.units.Swordsman;

public class Spearman extends Unit {
    public Spearman(Player owner, int row, int col) {
        super(owner,
                Constants.SPEARMAN_HEALTH,
                Constants.SPEARMAN_MOVEMENT_RANGE,
                Constants.SPEARMAN_ATTACK_POWER,
                Constants.SPEARMAN_ATTACK_RANGE,
                Constants.SPEARMAN_GOLD_COST,
                Constants.SPEARMAN_FOOD_COST,
                Constants.SPEARMAN_UNIT_SPACE_COST,
                Constants.SPEARMAN_HIERARCHY_LEVEL,
                row, col);
        GameLogger.log("Spearman created for " + owner.getName() + ".");
    }

    @Override
    public Unit mergeWith(Unit other) {
        if (other instanceof Spearman && this.owner == other.getOwner()) {
            GameLogger.log("Two Spearmen (owned by " + this.owner.getName() +
                    ") merged into a Swordsman.");
            this.owner.removeUnit(this);
            this.owner.removeUnit(other);
            return new Swordsman(this.owner, this.row, this.col);
        }
        GameLogger.logWarning("Cannot merge Spearman with " +
                (other != null ? other.getClass().getSimpleName() : "null") +
                ". Must be another Spearman owned by the same player.");
        return null;
    }
}