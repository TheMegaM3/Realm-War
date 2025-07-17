package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;

public class GameTile {

    private final Block block; // MODIFIED: Made private
    private GameEntity entity;
    private final int x;
    private final int y;
    private Player owner;

    // MODIFIED: Constructor now correctly takes x and y coordinates.
    public GameTile(Block b, int x, int y) {
        this.block = b;
        this.entity = null;
        this.x = x;
        this.y = y;
        this.owner = null;
    }

    public GameEntity getEntity() {
        return entity;
    }

    public void setEntity(GameEntity e) {
        this.entity = e;
        if (e instanceof Unit unit) {
            unit.setCurrentTile(this);
        }
        if (e != null) {
            this.owner = e.getOwner();
        }
    }

    public boolean isOccupied() {
        return this.entity != null;
    }

    // MODIFIED: Added missing getter methods.
    public Block getBlock() {
        return block;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
