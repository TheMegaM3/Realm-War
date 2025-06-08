package com.realmwar.engine;

import com.realmwar.engine.blocks.Block;
import com.realmwar.model.GameEntity;
public class GameTile {
    public final Block block; // Now holds a Block object, not an enum
    private GameEntity entity;
    public GameTile(Block b) { this.block = b; }
    public GameEntity getEntity() { return entity; }
    public void setEntity(GameEntity e) { this.entity = e; }
    public boolean isOccupied() { return this.entity != null; }
}
