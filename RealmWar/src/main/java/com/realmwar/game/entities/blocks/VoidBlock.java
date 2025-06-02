package realmwar.models.blocks;

import main.java.com.realmwar.game.entities.units.Unit;
import main.java.com.realmwar.game.entities.blocks.Block;
import main.java.com.realmwar.game.util.GameLogger;

public class VoidBlock extends Block {
    public VoidBlock(int  row, int col) {
        super(row, col);
        GameLogger.log("VoidBlock created at (" +  row + "," + col + ")");
    }

    @Override
    public boolean isAbsorbable(){
        return false;
    }

    @Override
    public boolean allowsBuilding(){
        return false;
    }

    @Override
    public void onUnitEnter(Unit unit){
        GameLogger.logWarning(unit.getClass().getSimpleName() + " attempted to enter VoidBlock at (" +  row + "," + col + "). This should not happen.");
    }
}
