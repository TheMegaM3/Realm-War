// EmptyBlock.java
// Represents a basic, buildable empty plain terrain in the RealmWar game.
// Extends the Block class to provide specific rendering and behavior.

package com.realmwar.engine.blocks;

import java.awt.Color;

// Concrete class representing an empty plain terrain block
public class EmptyBlock extends Block {
    // Returns the color for rendering this block (soft sandy beige)
    @Override
    public Color getColor() {
        return new Color(235, 224, 209); // Soft sandy beige
    }
}