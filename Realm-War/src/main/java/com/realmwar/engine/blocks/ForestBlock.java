package com.realmwar.engine.blocks;

import java.awt.Color;

/**
 * Represents a forest block, which provides combat modifiers.
 */
public class ForestBlock extends Block {
    @Override
    public Color getColor() {
        return new Color(188, 209, 188); // Muted sage green
    }
}
