// VoidBlock.java
// Represents an unbuildable, impassable void terrain in the RealmWar game.
// Extends the Block class to define a non-buildable terrain type.

package com.realmwar.engine.blocks;

import java.awt.*;

// Concrete class representing a void terrain block
public class VoidBlock extends Block {
    // Returns the color for rendering this block (black)
    @Override
    public Color getColor() {
        return Color.BLACK;
    }

    // Overrides to indicate that this block type is not buildable
    @Override
    public boolean isBuildable() {
        return false;
    }
}