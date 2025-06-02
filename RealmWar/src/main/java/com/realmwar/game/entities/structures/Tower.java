package main.java.com.realmwar.game.entities.structures;

import entities.units.*;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.Player;
import  main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import java.util.List;

public class Tower extends Structure {
    private int attackRange;
    private int attackPower;
    private int blockUnitBelowLevel;

    public Tower(Player owner, int row , int col) {
        super(owner ,Constants.TOWER_DURABILITY, Constants.TOWER_MAINTENANCE, 1 , Constants.TOWER_MAX_LEVEL, Constants.TOWER_GOLD_COST, Constants.TOWER_FOOD_COST, row, col);
        this.attackRange = Constants.TOWER_INITIAL_ATTACK_RANGE;
        this.attackPower = Constants.TOWER_INITIAL_ATTACK_POWER;
        this.blockUnitBelowLevel = Constants.TOWER_INITIAL_BLOCK_LEVEL;
        GameLogger.log("Tower Created for " + owner.getName() + " at (" + row + "," + col + "). Blocks unit below level " + blockUnitBelowLevel);
    }

    @Override
    public void upgrade(){
        if (this.level < this.maxLevel){
            this.level++;
            this.durability += Constants.TOWER_DURABILITY_PER_LEVEL;
            this.maxDurability += Constants.TOWER_DURABILITY_PER_LEVEL;
            this.attackPower += Constants.TOWER_ATTACK_POWER_PER_LEVEL;
            this.attackRange += Constants.TOWER_ATTACK_RANGE_PER_LEVEL;
            this.blockUnitBelowLevel += Constants.TOWER_BLOCK_LEVEL_PER_LEVEL;
            GameLogger.log("Tower upgraded to level " + this.level + ". New attack power: " + attackPower + ", blocks units below level: " + blockUnitBelowLevel);
        }else {
            GameLogger.logWarning("Tower is already at max level.");
        }
    }
    @Override
    public int getProductionAmount(){
        return 0;
    }

    public boolean blocksUnit(Unit unit){
        int unitLevel = getUnitHierarchyLevel();
        return unitLevel < this.blockUnitBelowLevel;
    }

    private int getUnitHierarchyLevel(Unit unit) {
        if (unit instanceof Peasant){
            return 1;
        }
        if (unit instanceof Spearman){
            return 2;
        }
        if (unit instanceof Swordsman){
            return 3;
        }
        if (unit instanceof Knight){
            return 4;
        }
        return 0;
    }

    public void defend(List<Unit> enemyUnits , List <Structure> enemyStructures){
        GameLogger.log("Tower at (" + row + "," + col + ") for " + owner.getName() + " is defending.");
        Unit targetUnit = findClosestTarget(enemyUnits);
        Structure targetStructure = findClosestTarget(enemyStructures);
        if (targetUnit != null && isUnitInRange(targetUnit)){
            targetUnit.takeDamage(this.attackPower);
            GameLogger.log("Tower attacked " + targetUnit.getClass().getSimpleName() + " at (" + targetUnit.getRow() + "," + targetUnit.getCol() + ")");
        }else if (targetStructure != null && isStructureInRange(targetStructure)){
            targetStructure.takeDamage(this.attackPower);
            GameLogger.log("Tower attacked " + targetStructure.getClass().getSimpleName() + " at (" + targetStructure.getRow() + "," + targetStructure.getCol() + ")");
        }else {
            GameLogger.log("No enemy targets in range for Tower at (" + row + "," + col + ")");
        }
    }

    private Unit findClosestTarget(List <Unit> units){
        Unit closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Unit unit : units){
            if (unit.getOwner() == this.owner){
                continue;
            }
            double distance = Math.sqrt(Math.pow(this.row - unit.getRow(), 2) + Math.pow(this.col - unit.getCol(), 2));
            if (distance <= this.attackRange && distance < minDistance){
                minDistance = distance;
                closest = unit;
            }
        }
        return closest;
    }

    private Structure findClosestTarget(List <Structure> structures){
        Structure closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Structure structure : structures){
            if (structure.getOwner() == this.owner){
                continue;
            }
            double distance = Math.sqrt(Math.pow(this.row - structure.getRow(), 2) + Math.pow(this.col - structure.getCol(), 2));
            if (distance <= this.attackRange && distance < minDistance){
                minDistance = distance;
                closest = structure;
            }
        }
        return closest;
    }

    private boolean isUnitInRange(Unit unit){
        double distance = Math.sqrt(Math.pow(this.row - unit.getRow(), 2) + Math.pow(this.col - unit.getCol(), 2));
        return distance <= this.attackRange;
    }

    private boolean isStructureInRange(Structure structure){
        double distance = Math.sqrt(Math.pow(this.row - structure.getRow(), 2) + Math.pow(this.col - structure.getCol(), 2));
        return distance <= this.attackRange;
    }
}
