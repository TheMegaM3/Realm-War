package main.java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.blocks.Block;
import main.java.com.realmwar.game.entities.blocks.VoidBlock;
import main.java.com.realmwar.game.entities.units.Knight;
import main.java.com.realmwar.game.entities.blocks.ForestBlock;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.units.Swordsman;
import main.java.com.realmwar.game.entities.units.Unit;

public class Spearman extends Unit {
    public Spearman(Player owner, int row , int col) {
        super(owner ,Constants.SPEARMAN_HEALTH , Constants.SPEARMAN_HEALTH,Constants.SPEARMAN_MOVEMENT_RANGE, Constants.SPEARMAN_ATTACK_POWER,Constants.PEASANT_MOVEMENT_RANGE, Constants.SPEARMAN_GOLD_COST ,Constants.SPEARMAN_FOOD_COST , Constants.SPEARMAN_UNIT_SPACE_COST,row,col);
        GameLogger.log("Spearman created for " + owner.getName());
    }

    @Override
    public void move(int targetRow, int targetCol) {
        GameManager gameManager = GameManager.getInstance();
        int distance = Math.abs(this.row - targetRow) + Math.abs(this.col - targetCol);
        if (distance > this.movementRange) {
            GameLogger.log("Spearman can't move to (" + targetRow + "," + targetCol + "): Out of Range.");
            return;
        }
        Block targetBlock = gameManager.getBlockAt(targetRow, targetCol);
        if (targetBlock == null || targetBlock instanceof VoidBlock){
            GameLogger.logError("Spearman can't move to (" + targetRow + "," + targetCol + "): Block occupied by another unit.");
            return;
        }
        if (targetBlock.getStructure() != null && targetBlock.getStructure().getOwner() != this.owner){
            GameLogger.logError("Spearman can't move onto enemy structure.");
            return;
        }
        gameManager.getBlockAt(this.row , this.col).setUnit(null);
        this.setPosition(targetRow, targetCol);
        targetBlock.setUnit(this);
        GameLogger.log("Spearman moved from (" + this.row + "," + this.col + ") to ( " + targetRow + "," + targetCol + ").");
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
        Block cuurentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);
        if(target instanceof Knight){
            finalAttackPower *= 1.5;
            GameLogger.log("Spearman uses anti-cavalry bonous against Knight.");
        }
        if (cuurentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Spearman gains forest advantage (+" + Constants.FOREST_ATTACK_ADVANTAGE + " attack)");
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }

    @Override
    public void attack(Structure target){
        if (this.owner == target.getOwner()){
            GameLogger.logWarning("Can't attack own structure.");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange){
            GameLogger.logError("Target structure out of range.");
            return;
        }
        int finalAttackPower = this.attackPower;
        Block curentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);
        if (curentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Spearman gains forest advantage (+ " + Constants.FOREST_ATTACK_ADVANTAGE + " attack)");
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }
    public Unit mergeWith(Spearman other){
        if (this.owner != other.owner){
            GameLogger.logWarning("Can't merge units from different players.");
            return null;
        }
        if (Math.abs(this.row - other.row) > 1 ||  Math.abs(this.col - other.col) > 1){
            GameLogger.logWarning("Units must be adjacent to merge.");
            return null;
        }
        this.owner.removeUnit(this);
        this.owner.removeUnit(other);
        GameLogger.log("Two Spearman merged into a Swordsman at (" + this.row + "," + this.col + ").");
        return new Swordsman(this.owner , this.row , this.col);
    }
    public void fortify(){
        this.attackPower += Constants.SPEARMAN_FORTIFY_BONUS;
        GameLogger.log("Spearman fortified position! Attack power creased to " + this.attackPower);
    }
}
