// Block.java
// Abstract base class for all terrain types on the game board in the RealmWar game.
// Provides a foundation for defining different types of terrain blocks with specific properties.

package com.realmwar.engine.blocks;

import java.awt.Color;

// Abstract base class for terrain blocks
public abstract class Block {
    // Returns the color used to render this block type in the game's UI
    public abstract Color getColor();

    // Determines if structures can be built on this block
    // Default implementation allows building, can be overridden by subclasses
    public boolean isBuildable() {
        return true;
    }
}