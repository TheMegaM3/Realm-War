// ForestBlock.java
// Represents a forest terrain block in the RealmWar game, which provides combat modifiers.
// Extends the Block class to define a specific terrain type with its own rendering color.

package com.realmwar.engine.blocks;

import java.awt.Color;

// Concrete class representing a forest terrain block
public class ForestBlock extends Block {
    // Returns the color for rendering this block (muted sage green)
    @Override
    public Color getColor() {
        return new Color(188, 209, 188); // Muted sage green
    }
}