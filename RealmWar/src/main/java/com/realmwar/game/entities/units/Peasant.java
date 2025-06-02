package realmwar.models.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.blocks.Block;
import realmwar.models.blocks.VoidBlock;
import realmwar.models.blocks.ForestBlock;
import main.java.com.realmwar.game.entities.structures.Structure;
import realmwar.models.units.Spearman;
import main.java.com.realmwar.game.entities.blocks.EmptyBlock;

public class Peasant extends Units {
    public Peasant(Player owner , int row, int col) {
        super(owner, Constants.PEASANT_HEALTH ,Constants.PEASANT_HEALTH, Constants.PEASANT_MOVEMENT_RANGE , Constants.PEASANT_ATTACK_POWER, Constants.PEASANT_ATTACK_RANGE , Constants.PEASANT_GOLD_COST, Constants.PEASANT_FOOD_COST, Constants.PEASANT_UNIT_SPACE_COST, row , col);
        GameLogger.log("Peasant created for " + owner.getName());
    }

    @Override
    public void move(int targetRow, int targetCol) {
        GameManager gameManager = GameManager.getInstance();
        int distance = Math.abs(this.row - targetRow) + Math.abs(this.col - targetCol);
        if (distance > this.movementRange){
            GameLogger.logError("Peasant can't move to (" + targetRow + "," + targetCol + "): Out of Range.");
            return;
        }
        Block targetBlock = gameManager.getBlockAt(targetRow, targetCol);
        if (targetBlock == null || targetBlock instanceof VoidBlock){
            GameLogger.logError("Peasant can't move to (" + targetRow + "," + targetCol + "): Invalid Block.");
            return;
        }
        if (targetBlock.getUnit() != null){
            GameLogger.logError("Peasant can't move to (" + targetRow + "," + targetCol + "): Block occupied by another unit.");
            return;
        }
        if (targetBlock.getStructure() != null && targetBlock.getStructure().getOwner() != this.owner){
            GameLogger.logError("Peasant can't move onto enemy structure.");
            return;
        }
        gameManager.getBlockAt(this.row , this.col).setUnit(null);
        this.setPosition(targetRow,targetCol);
        targetBlock.setUnit(this);
        GameLogger.log("Peasant moved to (" + this.row + "," + this.col + ") to (" + targetRow + ", " + targetCol + ")");
        //UI
    }

    @Override
    public void attck(Unit target) {
        if(this.owner == target.getOwner()){
            GameLogger.logWarning("Can't attack own unit.");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange){
            GameLogger.logError("Target unit out of range.");
            return;
        }
        int finalAttackPower = this.attackPower;
        Block currrentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);
        if (currrentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
        //UI
    }

    @Override
    public void attack(Structure target) {
        if (this.owner == target.getOwner()){
            GameLogger.logWarning("Can't attack own structure.");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange) {
            GameLogger.logError("Target Structure out of attack range.");
            return;
        }
        int finalAttackPower = this.attackPower;
        Block currrentBlock = GameManager.getInstance().getBlockAt(this.row , this.col);
        if (currrentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
        //UI
    }

    public Unit mergeWith(Peasant other){
        if(this.owner != other.owner){
            GameLogger.logWarning("Can't merge units from different players.");
            return null;
        }
        if (Math.abs(this.row - other.row) > 1 || Math.abs(this.col - other.col) > 1){
            GameLogger.logWarning("Units must be adjacent to merge.");
            return null;
        }
        this.owner.removeUnit(this);
        this.owner.removeUnit(other);
        GameLogger.log("Tow Peasant merge into a Spearman at (" + this.row + ", " + this.col + ").");
        return new Spearman(this.owner , this.row , this.col);
    }

    public void gatherResources(){
        Block currrentBlock = GameManager.getInstance().getBlockAt(this.row, this.col);
        if (currrentBlock instanceof EmptyBlock){
            this.owner.addGold(Constants.PEASANT_GATHER_AMOUNT);
            GameLogger.log("Peasant gathered "+ Constants.PEASANT_GATHER_AMOUNT + " gold");
        }
    }
}
