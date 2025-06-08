package com.realmwar.engine.blocks;

import java.awt.*;

public class VoidBlock extends Block {
        public Color getColor() { return Color.BLACK; }
        @Override public boolean isBuildable() { return false; }
    }
