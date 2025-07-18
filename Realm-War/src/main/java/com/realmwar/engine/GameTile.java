// GameTile.java
// Represents a single tile on the game board in the RealmWar game.
// Manages terrain type, entity occupancy, and territory ownership for a specific coordinate.

package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.model.Player;

// Class representing a single tile on the game board
public class GameTile {
    // The terrain type of the tile (e.g., Forest, Empty)
    public final Block block;
    // The unit or structure currently occupying this tile
    private GameEntity entity;
    // The x-coordinate of the tile
    private final int x;
    // The y-coordinate of the tile
    private final int y;
    // The player who owns this tile's territory
    private Player territoryOwner;

    // Constructor to initialize a tile with a terrain type and coordinates
    public GameTile(Block b, int x, int y) {
        this.block = b;
        this.entity = null; // Tile starts empty
        this.x = x;
        this.y = y;
        this.territoryOwner = null; // Initially no territory owner
    }

    // Gets the entity (unit or structure) on this tile
    public GameEntity getEntity() {
        return entity;
    }

    // Sets an entity on this tile and updates the entity's tile reference if it's a unit
    public void setEntity(GameEntity e) {
        this.entity = e;
        if (e instanceof Unit unit) {
            unit.setCurrentTile(this);
        }
    }

    // Checks if the tile is occupied by an entity
    public boolean isOccupied() {
        return this.entity != null;
    }

    // Gets the terrain block of this tile
    public Block getBlock() {
        return block;
    }

    // Gets the x-coordinate of the tile
    public int getX() { return x; }

    // Gets the y-coordinate of the tile
    public int getY() { return y; }

    // Gets the player who owns this tile's territory
    public Player getTerritoryOwner() {
        return territoryOwner;
    }

    // Sets the territory owner for this tile
    public void setTerritoryOwner(Player owner) {
        this.territoryOwner = owner;
    }
}