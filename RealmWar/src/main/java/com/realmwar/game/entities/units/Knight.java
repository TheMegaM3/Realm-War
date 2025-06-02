package java.com.realmwar.game.entities.units;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;
import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.entities.blocks.Block;
import main.java.com.realmwar.game.entities.blocks.EmptyBlock;
import realmwar.models.blocks.VoidBlock;
import realmwar.models.blocks.ForestBlock;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.units.Unit;
import java.util.List;

public class Knight extends Unit {
    public Knight(Player owner, int row, int col) {
        super(owner,Constants.KNIGHT_HEALTH,Constants.KNIGHT_HEALTH, Constants.KNIGHT_MOVEMENT_RANGE, Constants.KNIGHT_ATTACK_POWER, Constants.KNIGHT_ATTACK_RANGE,Constants.KNIGHT_GOLD_COST,Constants.KNIGHT_FOOD_COST,Constants.KNIGHT_UNIT_SPACE_COST,row , col);
        GameLogger.log("Knight created for " + owner.getName());
    }

    @Override
    public void move(int targetRow, int targetCol) {
        GameManager gameManager = GameManager.getInstance();
        Block targetBlock = gameManager.getBlockAt(targetRow, targetCol);
        int movementRange = this.movementRange;
        if(targetBlock instanceof EmptyBlock){
            movementRange += 1;
            GameLogger.log("Knight gains +1 movement on empty block");
        }
        int distance = Math.abs(this.row - targetRow) + Math.abs(this.col - targetCol);
        if(distance > movementRange){
            GameLogger.log("Knight can't move to (" + targetRow + "," + targetCol + "): Out of range");
            return;
        }
        if(targetBlock == null || targetBlock instanceof VoidBlock){
            GameLogger.logError("Knight can't move to (" + targetRow + "," + targetCol + "): Invalid block");
            return;
        }
        if (targetBlock.getUnit() != null) {
            GameLogger.logError("Knight can't move to (" + targetRow + "," + targetCol + "): Block occupied by another unit");
            return;
        }
        if (targetBlock.getStructure() != null && targetBlock.getStructure().getOwner() != this.owner){
            GameLogger.logError("Knight can't move onto enemy structure");
            return;
        }
        gameManager.getBlockAt(this.row , this.col).setUnit(null);
        this.setPosition(targetRow, targetCol);
        targetBlock.setUnit(this);
        GameLogger.log("Knight moved from ( " + this.row + "," + this.col + ") to ( " + targetRow + "," + targetCol + ")");
    }

    @Override
    public void attack(Unit target){
        if(this.owner == target.getOwner()){
            GameLogger.logWarning("Can't attack own unit");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if(distance > this.attackRange){
            GameLogger.logError("Target unit out of range.");
            return;
        }
        int finalAttackPower = this.attackPower;
        Block currentBlock = GameManager.getInstance().getBlockAt(this.row , this.col);
        if(target instanceof Peasant){
            finalAttackPower *=2;
            GameLogger.log("Knight uses peasant-crushing strike!");
        }
        if (currentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Knight gains forest advantage (+" + Constants.FOREST_ATTACK_ADVANTAGE + " attack");
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }

    @Override
    public void attack(Structure target){
        if (this.owner == target.getOwner()){
            GameLogger.logWarning("Can't attack own structure");
            return;
        }
        int distance = Math.abs(this.row - target.getRow()) + Math.abs(this.col - target.getCol());
        if (distance > this.attackRange){
            GameLogger.logError("Target structure out of range.");
            return;
        }
        int finalAttackPower = this.attackPower;
        Block currentBlock = GameManager.getInstance().getBlockAt(this.row , this.col);
        if (currentBlock instanceof ForestBlock){
            finalAttackPower += Constants.FOREST_ATTACK_ADVANTAGE;
            GameLogger.log("Knight gains forest advantage (+" + Constants.FOREST_ATTACK_ADVANTAGE + " attack)");
        }
        GameLogger.log(this.getClass().getSimpleName() + " attacks " + target.getClass().getSimpleName() + " with " + finalAttackPower + " power.");
        target.takeDamage(finalAttackPower);
    }

    public void whirlwindAttack(List<Unit>targets){
        if (targets == null || targets.isEmpty()){
            GameLogger.logWarning("No targets for whirlwind attack");
            return;
        }
        for (Unit target : targets){
            if(target.getOwner() != this.owner){
                this.attack(target);
            }
        }
        GameLogger.log("Knight performed whirlwind attack on " + targets.size() + " targets.");
    }
}
