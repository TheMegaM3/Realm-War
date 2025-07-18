// Market.java
// Represents a Market structure in the RealmWar game, which produces gold resources.
// Extends the Structure class to include gold production logic based on level.

package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

// Class representing a Market structure
public class Market extends Structure {
    // Constructor to initialize a Market with owner, position, and default attributes
    public Market(Player owner, int x, int y) {
        super(owner, x, y, Constants.MARKET_DURABILITY, Constants.MARKET_MAINTENANCE);
    }

    // Calculates gold production based on the market's level
    public int getGoldProduction() {
        return Constants.MARKET_GOLD_PER_TICK + (level - 1) * Constants.MARKET_GOLD_INCREMENT_PER_LEVEL;
    }
}