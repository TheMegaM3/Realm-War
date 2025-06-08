package com.realmwar.model.units;

import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;
public class Spearman extends Unit {
    public Spearman(Player o, int x, int y) {
        super(o, x, y, Constants.SPEARMAN_HEALTH, Constants.SPEARMAN_ATTACK, Constants.SPEARMAN_RANGE, Constants.SPEARMAN_MOVE); }
}