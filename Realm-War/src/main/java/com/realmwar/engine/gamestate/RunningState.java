// RunningState.java
// Represents the active game state in the RealmWar game where gameplay actions are allowed.
// Implements the GameState interface to handle unit movements, attacks, and turn progression.

package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

// Concrete game state class for when the game is in progress
public class RunningState extends GameState {

    // Constructor to initialize with GameManager
    public RunningState(GameManager gm) {
        super(gm);
    }

    // Handles unit movement by delegating to the GameManager
    @Override
    public void moveUnit(Unit u, int x, int y) throws GameRuleException {
        // Delegates the action to the game manager's logic.
        gameManager.executeMove(u, x, y);
    }

    // Handles unit attacks by delegating to the GameManager
    @Override
    public void attackUnit(Unit a, GameEntity t) throws GameRuleException {
        // Delegates the action to the game manager's logic.
        gameManager.executeAttack(a, t);
    }

    // Advances to the next turn by delegating to the GameManager
    @Override
    public void nextTurn() {
        gameManager.advanceTurn();
    }

    // Returns the game status indicating active gameplay
    @Override
    public String getStatus() {
        return "Game in Progress";
    }
}