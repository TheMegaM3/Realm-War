package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;

public class Barrack extends Structure {
    private int unitSpace;

    public Barrack(Player o, int x, int y) {
        super(o, x, y, Constants.BARRACK_DURABILITY, Constants.BARRACK_MAINTENANCE);
        this.unitSpace = Constants.BARRACK_BASE_UNIT_SPACE;
    }

    /**
     * Calculates the unit space based on the structure's level.
     * @return The current unit space capacity.
     */
    // MODIFIED: This method was missing, causing a compilation error in Player.java.
    public int getUnitSpace() {
        return this.unitSpace;
    }

    @Override
    public void levelUp() {
        if (level < maxLevel) {
            super.levelUp();
            // Increase unit space capacity with each level up.
            this.unitSpace += Constants.BARRACK_UNIT_SPACE_INCREMENT;
        }
    }
}
