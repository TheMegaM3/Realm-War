package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;

/**
 * Represents a single tile on the game board.
 * A tile has a terrain type (Block), can hold one GameEntity, and can have an owner.
 */
public class GameTile {

    public final Block block; // The terrain type of the tile (e.g., Forest, Empty).
    private GameEntity entity; // The unit or structure currently on this tile.
    private Player owner;      // The player who currently controls this tile.

    public GameTile(Block b) {
        this.block = b;
        this.entity = null; // A tile is created empty.
        this.owner = null;  // A tile is created neutral.
    }

    public GameEntity getEntity() {
        return entity;
    }

    public void setEntity(GameEntity e) {
        this.entity = e;
    }

    public boolean isOccupied() {
        return this.entity != null;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
