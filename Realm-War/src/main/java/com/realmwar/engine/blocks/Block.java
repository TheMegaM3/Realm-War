package com.realmwar.engine.blocks;

import java.awt.Color;
public abstract class Block {
    public abstract Color getColor();
    public boolean isBuildable() { return true; }
}