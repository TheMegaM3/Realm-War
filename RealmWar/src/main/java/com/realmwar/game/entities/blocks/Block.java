package main.java.com.realmwar.game.entities.blocks;

import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.units.Unit;

public abstract class Block {
    protected int row;
    protected int col;
    protected Structure structure;
    protected Unit unit;

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
