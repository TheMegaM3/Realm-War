package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;
public class Peasant extends Unit {
    public Peasant(Player o, int x, int y) {
        super(o, x, y, Constants.PEASANT_HEALTH, Constants.PEASANT_ATTACK, Constants.PEASANT_RANGE, Constants.PEASANT_MOVE); }
}