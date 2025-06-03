package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.entities.units.Knight;

public class Swordsman extends Unit {
    public Swordsman(Player owner, int row, int col) {
        super(owner,
                Constants.SWORDSMAN_HEALTH,
                Constants.SWORDSMAN_MOVEMENT_RANGE,
                Constants.SWORDSMAN_ATTACK_POWER,
                Constants.SWORDSMAN_ATTACK_RANGE,
                Constants.SWORDSMAN_GOLD_COST,
                Constants.SWORDSMAN_FOOD_COST,
                Constants.SWORDSMAN_UNIT_SPACE_COST,
                Constants.SWORDSMAN_HIERARCHY_LEVEL,
                row, col);
        GameLogger.log("Swordsman created for " + owner.getName() + ".");
    }

    @Override
    public Unit mergeWith(Unit other) {
        if (other instanceof Swordsman && this.owner == other.getOwner()) {
            GameLogger.log("Two Swordsmen (owned by " + this.owner.getName() +
                    ") merged into a Knight.");
            this.owner.removeUnit(this);
            this.owner.removeUnit(other);
            return new Knight(this.owner, this.row, this.col);
        }
        GameLogger.logWarning("Cannot merge Swordsman with " +
                (other != null ? other.getClass().getSimpleName() : "null") +
                ". Must be another Swordsman owned by the same player.");
        return null;
    }
}