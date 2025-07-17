package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;
public class Tower extends Structure {
    public Tower(Player o, int x, int y) {
        super(o, x, y, Constants.TOWER_DURABILITY, Constants.TOWER_MAINTENANCE); }

    public int getAttackPower() { return Constants.TOWER_ATTACK_POWER; }
    public int getAttackRange() { return Constants.TOWER_ATTACK_RANGE; }

    public boolean blocksUnit(Unit unit) {
        return this.getLevel() >= unit.getUnitLevel();
    }
}


