package com.realmwar.model.units;

import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;

public abstract class Unit extends GameEntity {
    public int health;
    public final int maxHealth;
    public final int attackPower;
    public final int attackRange;
    public final int movementRange;
    private boolean hasActedThisTurn=false;
    public Unit(Player o, int x, int y, int h, int a, int ar, int mr) {
        super(o,x,y);
        this.maxHealth=h;
        this.health=h;
        this.attackPower=a;
        this.attackRange=ar;
        this.movementRange=mr;
    }
    public int getHealth() {
        return health; }

    public int getMaxHealth() {
        return maxHealth; }

    public int getAttackPower() {
        return attackPower; }

    public int getAttackRange() {
        return attackRange; }

    public int getMovementRange() {
        return movementRange; }

    public boolean hasActedThisTurn() {
        return hasActedThisTurn; }

    public void setHasActedThisTurn(boolean value) {
        this.hasActedThisTurn=value; }

    @Override public void takeDamage(int amount) {
        this.health-=amount; if(this.health<0) this.health=0; }

    @Override public boolean isDestroyed() {
        return this.health<=0; }
}
