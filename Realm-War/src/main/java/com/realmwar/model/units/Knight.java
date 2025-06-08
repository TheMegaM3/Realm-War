package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;
public class Knight extends Unit {
    public Knight(Player o, int x, int y) {
        super(o, x, y, Constants.KNIGHT_HEALTH, Constants.KNIGHT_ATTACK, Constants.KNIGHT_RANGE, Constants.KNIGHT_MOVE); }
}
