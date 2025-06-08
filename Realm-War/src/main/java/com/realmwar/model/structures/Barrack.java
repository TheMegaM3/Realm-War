package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.util.Constants;
public class Barrack extends Structure {
    public Barrack(Player o, int x, int y) {
        super(o, x, y, Constants.BARRACK_DURABILITY, Constants.BARRACK_MAINTENANCE); }
}