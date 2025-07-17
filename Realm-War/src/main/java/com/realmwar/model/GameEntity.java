package com.realmwar.model;

import com.realmwar.engine.GameBoard;

public abstract class GameEntity {

    protected Player owner;
    protected int x, y;

    public GameEntity(Player owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;
    }

    public Player getOwner() {
        return owner;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isDestroyed() {
        return false;
    }

    // MODIFIED: Method signature changed to accept a GameBoard.
    // This allows subclasses to check for game conditions, like adjacent towers.
    public void takeDamage(int damage, GameBoard board) {
        // Base entities take no damage by default.
    }
}
