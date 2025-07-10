package com.realmwar.model.structures;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.util.Constants;
public class TownHall extends Structure {
    public TownHall(Player o, int x, int y) {
        super(o, x, y, Constants.TOWNHALL_DURABILITY, 0); }
}