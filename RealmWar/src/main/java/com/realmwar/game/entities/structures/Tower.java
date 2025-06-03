package main.java.com.realmwar.game.entities.structures;

import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.entities.units.Unit;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import java.util.List;

public class Tower extends Structure {
    private int attackRange;
    private int attackPower;
    private int blocksUnitsBelowLevel;

    public Tower(Player owner, int row, int col) {
        super(owner, Constants.TOWER_DURABILITY,
                Constants.TOWER_MAINTENANCE, 1, Constants.TOWER_MAX_LEVEL,
                Constants.TOWER_GOLD_COST, Constants.TOWER_FOOD_COST, row, col);
        this.attackRange = Constants.TOWER_INITIAL_ATTACK_RANGE;
        this.attackPower = Constants.TOWER_INITIAL_ATTACK_POWER;
        this.blocksUnitsBelowLevel = Constants.TOWER_INITIAL_BLOCK_LEVEL;
        GameLogger.log("Tower created for " + owner.getName() + " at (" + row + ", " + col + "). Blocks units below level " + blocksUnitsBelowLevel + ".");
    }

    @Override
    public void upgrade() {
        if (this.level < this.maxLevel) {
            this.level++;
            this.durability += Constants.TOWER_DURABILITY_PER_LEVEL;
            this.maxDurability += Constants.TOWER_DURABILITY_PER_LEVEL;
            this.attackPower += Constants.TOWER_ATTACK_POWER_PER_LEVEL;
            this.attackRange += Constants.TOWER_ATTACK_RANGE_PER_LEVEL;
            this.blocksUnitsBelowLevel += Constants.TOWER_BLOCK_LEVEL_PER_LEVEL;
            GameLogger.log("Tower upgraded to level " + this.level + ". New attack power: " + attackPower + ", blocks units below level: " + blocksUnitsBelowLevel + ".");
        } else {
            GameLogger.logWarning("Tower is already at max level.");
        }
    }

    @Override
    public int getProductionAmount() {
        return 0;
    }

    public boolean blocksUnit(Unit unit) {
        return unit.getHierarchyLevel() < this.blocksUnitsBelowLevel;
    }

    public void defend() {
        GameLogger.log("Tower at (" + row + "," + col + ") for " + owner.getName() + " is defending.");
        GameManager gm = GameManager.getInstance();
        List<Unit> allUnits = gm.getAllUnitsOnBoard();
        List<Structure> allStructures = gm.getAllStructuresOnBoard();

        Unit targetUnit = null;
        Structure targetStructure = null;
        double minDistUnit = Double.MAX_VALUE;
        double minDistStructure = Double.MAX_VALUE;

        for (Unit unit : allUnits) {
            if (unit.getOwner() != this.owner) {
                double dist = Math.sqrt(Math.pow(this.row - unit.getRow(), 2) +
                        Math.pow(this.col - unit.getCol(), 2));
                if (dist <= this.attackRange && dist < minDistUnit) {
                    minDistUnit = dist;
                    targetUnit = unit;
                }
            }
        }

        for (Structure structure : allStructures) {
            if (structure.getOwner() != this.owner && !(structure instanceof Tower)) {
                double dist = Math.sqrt(Math.pow(this.row - structure.getRow(), 2) +
                        Math.pow(this.col - structure.getCol(), 2));
                if (dist <= this.attackRange && dist < minDistStructure) {
                    minDistStructure = dist;
                    targetStructure = structure;
                }
            }
        }

        if (targetUnit != null && (targetStructure == null || minDistUnit <= minDistStructure)) {
            try {
                targetUnit.takeDamage(this.attackPower);
                GameLogger.log("Tower at (" + row + "," + col + ") attacked " +
                        targetUnit.getClass().getSimpleName() + " at (" + targetUnit.getRow() + "," +
                        targetUnit.getCol() + ") for " + this.attackPower + " damage.");
            } catch (Exception e) {
                GameLogger.logError("Tower attack failed on unit: " + e.getMessage());
            }
        } else if (targetStructure != null) {
            try {
                targetStructure.takeDamage(this.attackPower);
                GameLogger.log("Tower at (" + row + "," + col + ") attacked " +
                        targetStructure.getClass().getSimpleName() + " at (" + targetStructure.getRow() + "," +
                        targetStructure.getCol() + ") for " + this.attackPower + " damage.");
            } catch (Exception e) {
                GameLogger.logError("Tower attack failed on structure: " + e.getMessage());
            }
        } else {
            GameLogger.log("No enemy targets in range for Tower at (" + row + "," + col + ").");
        }
    }
}