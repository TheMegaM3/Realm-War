package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;

public class Barrack extends Structure {
    private int unitSpace;

    public Barrack(Player o, int x, int y) {
        super(o, x, y, Constants.BARRACK_DURABILITY, Constants.BARRACK_MAINTENANCE);
        this.unitSpace = Constants.BARRACK_BASE_UNIT_SPACE; // ظرفیت اولیه 4
    }

    /**
     * Calculates the unit space based on the structure's level.
     * @return The current unit space capacity.
     */
    public int getUnitSpace() {
        return unitSpace;
    }

    /**
     * Returns the valid directions where units can be placed based on the barrack's level.
     * Level 1: Up, Down, Left, Right (4 directions)
     * Level 2: Adds Northwest, Northeast (6 directions)
     * Level 3: Adds Southwest, Southeast (8 directions)
     * @return List of Points representing relative coordinates of valid directions.
     */
    public List<Point> getValidUnitPlacementDirections() {
        return switch (level) {
            case 1 -> Arrays.asList(
                    new Point(0, -1), // Up
                    new Point(0, 1),  // Down
                    new Point(-1, 0), // Left
                    new Point(1, 0)   // Right
            );
            case 2 -> Arrays.asList(
                    new Point(0, -1), // Up
                    new Point(0, 1),  // Down
                    new Point(-1, 0), // Left
                    new Point(1, 0),  // Right
                    new Point(-1, -1), // Northwest
                    new Point(1, -1)   // Northeast
            );
            case 3 -> Arrays.asList(
                    new Point(0, -1), // Up
                    new Point(0, 1),  // Down
                    new Point(-1, 0), // Left
                    new Point(1, 0),  // Right
                    new Point(-1, -1), // Northwest
                    new Point(1, -1),  // Northeast
                    new Point(-1, 1),  // Southwest
                    new Point(1, 1)    // Southeast
            );
            default -> Arrays.asList(); // No valid directions for invalid level
        };
    }

    @Override
    public void levelUp() {
        if (level < maxLevel) {
            super.levelUp();
            this.unitSpace += Constants.BARRACK_UNIT_SPACE_INCREMENT; // افزایش ظرفیت با هر ارتقاء
        }
    }
}