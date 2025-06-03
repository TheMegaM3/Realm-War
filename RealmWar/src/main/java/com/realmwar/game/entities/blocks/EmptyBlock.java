package main.java.com.realmwar.game.entities.blocks;

import main.java.com.realmwar.game.entities.units.Unit;
import main.java.com.realmwar.game.util.GameLogger;

public class EmptyBlock extends Block {
    public EmptyBlock(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isAbsorbable() {
        return true;
    }

    @Override
    public boolean allowsBuilding() {
        return true;
    }

    @Override
    public void onUnitEnter(Unit unit) {
        GameLogger.log(unit.getClass().getSimpleName() + " entered EmptyBlock at (" +
                row + "," + col + ").");
    }
}