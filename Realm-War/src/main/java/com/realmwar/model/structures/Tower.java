// Tower.java
// Represents a Tower structure in the RealmWar game, which can attack enemy units and block unit movement.
// Extends the Structure class to include attack capabilities and unit blocking logic.

package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;

// Class representing a Tower structure
public class Tower extends Structure {
    // Constructor to initialize a Tower with owner, position, and default attributes
    public Tower(Player o, int x, int y) {
        super(o, x, y, Constants.TOWER_DURABILITY, Constants.TOWER_MAINTENANCE);
    }

    // Gets the attack power of the tower
    public int getAttackPower() { return Constants.TOWER_ATTACK_POWER; }

    // Gets the attack range of the tower
    public int getAttackRange() { return Constants.TOWER_ATTACK_RANGE; }

    // Determines if the tower blocks a unit based on its level
    public boolean blocksUnit(Unit unit) {
        return this.getLevel() >= unit.getUnitLevel();
    }
}