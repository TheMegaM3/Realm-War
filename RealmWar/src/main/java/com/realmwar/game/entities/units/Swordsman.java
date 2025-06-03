package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.blocks.Block;
import main.java.com.realmwar.game.entities.blocks.VoidBlock;
import main.java.com.realmwar.game.entities.blocks.ForestBlock;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.structures.Tower;
import main.java.com.realmwar.game.entities.units.Knight;
import main.java.com.realmwar.game.entities.units.Unit;
import java.util.List;

public class Swordsman extends Unit {
    public Swordsman(Player owner , int row , int col) {
        super(owner , Constants.SWORDSMAN_HEALTH,Constants.SPEARMAN_HEALTH, Constants.SWORDSMAN_MOVEMENT_RANGE,Constants.SPEARMAN_ATTACK_POWER,Constants.SWORDSMAN_ATTACK_RANGE, Constants.SWORDSMAN_GOLD_COST,Constants.SWORDSMAN_FOOD_COST, Constants.SWORDSMAN_UNIT_SPACE_COST,row,col);
        GameLogger.log("Swordsman created for " + owner.getName());
    }

    @Override
    public void move(int targetRow , int targetCol) {
        GameManager gameManager = GameManager.getInstance();
        int distance = Math.abs(this.row - targetRow) + Math.abs(this.col - targetCol);
        if (distance > this.movementRange) {
            GameLogger.logError("Swordsman can't  move to (" + targetRow + "," + targetCol + "): Out of range.");
            return;
        }
        Block targetBlock = gameManager.getBlockAt(targetRow, targetCol);
        if (targetBlock == null || targetBlock instanceof VoidBlock) {
            GameLogger.logError("Swordsman can't move to (" + targetRow + "," + targetCol + "): Block occupied by another unit.");
            return;
        }
        if (targetBlock.getStructure() != null && targetBlock.getStructure().getOwner() != this.owner) {
            GameLogger.logError("Swordsman can't move onto enemy structure.");
            return;
        }
        gameManager.getBlockAt(this.row, this.col).setUnit(null);
        this.setPosition(targetRow, targetCol);
        targetBlock.setUnit(this);
        GameLogger.log("Swordsman moved from ( " + this.row + "," + this.col + ") to (" + targetRow + "," + targetCol + ").");
    }

    @Override
    public void attack(Unit target){
        if (this.owner == target.getOwner()){
            GameLogger.logWarning("Can't attack own unit.");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange){
            GameLogger.logError("Target unit out of range.");
            return;
        }
        int finalAttackPower = this.attackPower;
        Block currentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);
        if (currentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Swordsman gains forest advantage (+" + Constants.FOREST_ATTACK_ADVANTAGE + " attack)");
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }

    @Override
    public void attack(Structure target) {
        if (this.owner == target.getOwner()) {
            GameLogger.logWarning("Can't attack own structure.");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange) {
            GameLogger.logError("Target unit out of range.");
            return;
        }
        if (target instanceof Tower) {
            Tower tower = (Tower) target;
            if (tower.getLevel() <= 2) {
                int damage = this.attackPower * 2;
                GameLogger.log("Swordsman bypasses tower defenses! Deals " + damage + " damage");
                tower.takeDamage(damage);
                return;
            }
        }
        int finalAttackPower = this.attackPower;
        Block currentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);
        if (currentBlock instanceof ForestBlock) {
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Swordsman gains forest advantage (+" + Constants.FOREST_ATTACK_ADVANTAGE + " attack");
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks "+ target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }
    public Unit mergeWith(Swordsman other) {
        if (this.owner != other.owner) {
            GameLogger.logWarning("Can't merge units from different players.");
            return null;
        }
        if (Math.abs(this.row - other.row) > 1 ||  Math.abs(this.col - other.col) > 1) {
            GameLogger.logWarning("Units must be adjacent to merge.");
            return null;
        }
        this.owner.removeUnit(this);
        this.owner.removeUnit(other);
        GameLogger.log("Two Swordsman merge into a knight at (" + this.row + "," + this.col + ").");
        return new Knight(this.owner , this.row , this.col);
    }
    public void cleaveAttack(Unit primaryTarget) {
        this.attack(primaryTarget);
        List<Unit> adjacentUnits = GameManager.getInstance().getAdjacentUnits(this.row , this.col);
        for (Unit unit : adjacentUnits) {
            if(unit != primaryTarget && unit.getOwner() != this.owner){
                unit.takeDamage(this.attackPower / 2);
                GameLogger.log("Cleave attack hits " + unit.getClass().getSimpleName() + " for " + (this.attackPower / 2) + " damage");
            }
        }
    }
}
