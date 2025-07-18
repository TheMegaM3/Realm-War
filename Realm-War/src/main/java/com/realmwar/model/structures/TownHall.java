// TownHall.java
// Represents a TownHall structure in the RealmWar game, a critical structure for each player.
// Extends the Structure class with specific attributes for durability and maintenance.

package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.util.Constants;

// Class representing a TownHall structure
public class TownHall extends Structure {
    // Constructor to initialize a TownHall with owner, position, and default attributes
    public TownHall(Player o, int x, int y) {
        super(o, x, y, Constants.TOWNHALL_DURABILITY, 0);
    }
}