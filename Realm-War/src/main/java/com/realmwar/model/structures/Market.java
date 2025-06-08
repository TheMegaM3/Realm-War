package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.util.Constants;
public class Market extends Structure {
    public Market(Player o, int x, int y) {
        super(o, x, y, Constants.MARKET_DURABILITY, Constants.MARKET_MAINTENANCE); }
}