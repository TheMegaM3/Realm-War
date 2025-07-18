// GameState.java
// Abstract base class for the State Design Pattern in the RealmWar game.
// Defines the interface for different game states (e.g., Running, GameOver) and their behaviors.

package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

// Abstract base class for game states
public abstract class GameState {
    // Reference to the GameManager for accessing game logic
    protected GameManager gameManager;

    // Constructor to initialize with GameManager
    public GameState(GameManager gm) {
        this.gameManager = gm;
    }

    // Defines the behavior for moving a unit
    public abstract void moveUnit(Unit u, int x, int y) throws GameRuleException;

    // Defines the behavior for attacking with a unit
    public abstract void attackUnit(Unit a, GameEntity t) throws GameRuleException;

    // Defines the behavior for advancing to the next turn
    public abstract void nextTurn();

    // Returns the current status of the game
    public abstract String getStatus();
}