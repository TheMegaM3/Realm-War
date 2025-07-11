package com.realmwar.engine.blocks;

import java.awt.Color;

/**
 * Represents a basic, buildable empty plain.
 */
public class EmptyBlock extends Block {
    @Override
    public Color getColor() {
        return new Color(235, 224, 209); // Soft sandy beige
    }
}