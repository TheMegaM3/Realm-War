package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;

public class GameTile {
    public final Block block;
    private GameEntity entity;
    private Player owner; // مالک این کاشی

    public GameTile(Block b) {
        this.block = b;
        this.entity = null;
        this.owner = null;
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
