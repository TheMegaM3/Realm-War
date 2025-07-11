package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

/**
 * Abstract base class for the State Design Pattern.
 * Defines the actions that can change based on the game's current state (e.g., Running vs. GameOver).
 */
public abstract class GameState {
    protected GameManager gameManager;

    public GameState(GameManager gm) {
        this.gameManager = gm;
    }

    public abstract void moveUnit(Unit u, int x, int y) throws GameRuleException;
    public abstract void attackUnit(Unit a, GameEntity t) throws GameRuleException;
    public abstract void nextTurn();
    public abstract String getStatus();
}
