package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;

/**
 * Represents a single tile on the game board.
 * A tile has a terrain type (Block) and can hold one GameEntity.
 */
public class GameTile {

    public final Block block; // The terrain type of the tile (e.g., Forest, Empty).
    private GameEntity entity; // The unit or structure currently on this tile.
    private final int x;
    private final int y;

    public GameTile(Block b, int x, int y) {
        this.block = b;
        this.entity = null; // A tile is created empty.
        this.x = x;
        this.y = y;
    }

    public GameEntity getEntity() {
        return entity;
    }

    public void setEntity(GameEntity e) {
        this.entity = e;
        if (e instanceof Unit unit) {
            unit.setCurrentTile(this);
        }
    }

    public boolean isOccupied() {
        return this.entity != null;
    }

    public Block getBlock() {
        return block;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}