package com.realmwar.model;
public abstract class GameEntity {
    protected Player owner;
    protected int x, y;

    public GameEntity(Player owner, int x, int y) {
        this.owner=owner;
        this.x=x;
        this.y=y;
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
        this.x=x;
        this.y=y;
    }
    public boolean isDestroyed() {
        return false;
    } // Default for entities that can't be destroyed
    public void takeDamage(int damage) {} // Default for entities that can't take damage
}
