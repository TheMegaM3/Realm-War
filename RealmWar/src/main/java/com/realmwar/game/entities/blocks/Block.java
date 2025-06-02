package main.java.com.realmwar.game.entities.blocks;

import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.units.Unit;
import main.java.com.realmwar.game.util.GameLogger;
public abstract class Block {
    protected int row;
    protected int col;
    protected Structure structure;
    protected Unit unit;

    public Block(int row, int col) {
        this.row = row;
        this.col = col;
        this.structure = null;
        this.unit = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Structure getStructure() {
        return structure;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
        GameLogger.log("Structure " + (structure != null ? structure.getClass().getSimpleName() : "None") + " set on block (" + row + "," + col + ")");
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
        GameLogger.log("Unit " + (unit != null ? unit.getClass().getSimpleName() : "None") + " set on block (" + row + "," + col + ")");
    }

    public abstract boolean isAbsorbable();

    public abstract boolean allowsBuilding();

    public abstract void onUnitEnter(Unit unit);

}
