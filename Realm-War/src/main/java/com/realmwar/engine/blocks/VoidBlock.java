package com.realmwar.engine.blocks;

import java.awt.*;

/**
 * Represents an unbuildable, impassable void block.
 */
public class VoidBlock extends Block {
    @Override
    public Color getColor() {
        return Color.BLACK;
    }

    @Override
    public boolean isBuildable() {
        return false;
    }
}
