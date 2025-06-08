package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.util.Constants;
public class Farm extends Structure {
    public Farm(Player o, int x, int y) {
        super(o, x, y, Constants.FARM_DURABILITY, Constants.FARM_MAINTENANCE); }
}