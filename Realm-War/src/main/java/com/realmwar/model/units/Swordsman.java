package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;

public class Swordsman extends Unit {
    public Swordsman(Player o, int x, int y) { super(o, x, y, Constants.SWORDSMAN_HEALTH, Constants.SWORDSMAN_ATTACK, Constants.SWORDSMAN_RANGE, Constants.SWORDSMAN_MOVE); }
}
