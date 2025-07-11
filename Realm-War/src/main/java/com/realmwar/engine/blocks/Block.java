package com.realmwar.engine.blocks;

import java.awt.Color;

/**
 * Abstract base class for all terrain types on the game board.
 */
public abstract class Block {
    /**
     * @return The color used to render this block type.
     */
    public abstract Color getColor();

    /**
     * @return true if structures can be built on this block, false otherwise.
     */
    public boolean isBuildable() {
        return true;
    }
}