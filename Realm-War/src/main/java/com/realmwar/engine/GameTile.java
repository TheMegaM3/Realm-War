package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.model.Player;

/**
 * Represents a single tile on the game board.
 * A tile has a terrain type (Block), can hold one GameEntity, and tracks territory ownership.
 */
public class GameTile {

    public final Block block; // The terrain type of the tile (e.g., Forest, Empty).
    private GameEntity entity; // The unit or structure currently on this tile.
    private final int x;
    private final int y;
    private Player territoryOwner; // The player who owns this tile's territory.

    public GameTile(Block b, int x, int y) {
        this.block = b;
        this.entity = null; // A tile is created empty.
        this.x = x;
        this.y = y;
        this.territoryOwner = null; // Initially, no player owns the territory.
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

    public Player getTerritoryOwner() {
        return territoryOwner;
    }

    public void setTerritoryOwner(Player owner) {
        this.territoryOwner = owner;
    }
}