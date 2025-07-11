package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

/**
 * Represents the state where the game is actively in progress.
 * In this state, actions like moving and attacking are allowed.
 */
public class RunningState extends GameState {

    public RunningState(GameManager gm) {
        super(gm);
    }

    @Override
    public void moveUnit(Unit u, int x, int y) throws GameRuleException {
        // Delegates the action to the game manager's logic.
        gameManager.executeMove(u, x, y);
    }

    @Override
    public void attackUnit(Unit a, GameEntity t) throws GameRuleException {
        // Delegates the action to the game manager's logic.
        gameManager.executeAttack(a, t);
    }

    @Override
    public void nextTurn() {
        gameManager.advanceTurn();
    }

    @Override
    public String getStatus() {
        return "Game in Progress";
    }
}