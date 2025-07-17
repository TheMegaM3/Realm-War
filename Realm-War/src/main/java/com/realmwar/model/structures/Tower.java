package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;

public class Tower extends Structure {
    public Tower(Player o, int x, int y) {
        super(o, x, y, Constants.TOWER_DURABILITY, Constants.TOWER_MAINTENANCE);
    }


    // high-level towers can still function as "walls"
    // to block the movement of low-level enemy units. This is a form of protection.
    public boolean blocksUnit(Unit unit) {
        return this.getLevel() >= unit.getUnitLevel();
    }
}
