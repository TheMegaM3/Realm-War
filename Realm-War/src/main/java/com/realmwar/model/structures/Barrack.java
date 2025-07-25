package com.realmwar.model.structures;

import com.realmwar.model.Player;
import com.realmwar.util.Constants;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;

// Class representing a Barrack structure
public class Barrack extends Structure {
    // The unit space capacity provided by this barrack
    private int unitSpace;

    // Constructor to initialize a Barrack with owner, position, and default attributes
    public Barrack(Player o, int x, int y) {
        super(o, x, y, Constants.BARRACK_DURABILITY, Constants.BARRACK_MAINTENANCE);
        this.unitSpace = Constants.BARRACK_BASE_UNIT_SPACE; // Initial unit space capacity
    }

    // Gets the current unit space capacity
    public int getUnitSpace() {
        return unitSpace;
    }

    // Returns valid directions for unit placement based on the barrack's level
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

    // Upgrades the barrack, increasing level and unit space
    @Override
    public void levelUp() {
        if (level < maxLevel) {
            super.levelUp();
            this.unitSpace += Constants.BARRACK_UNIT_SPACE_INCREMENT; // Increase unit space on upgrade
        }
    }
}