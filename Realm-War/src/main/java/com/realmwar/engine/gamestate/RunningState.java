package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

public class RunningState extends GameState {

    public RunningState(GameManager gm) {
        super(gm);
    }

    @Override public void moveUnit(Unit u, int x, int y) throws GameRuleException {
        gameManager.executeMove(u,x,y);
    }

    @Override public void attackUnit(Unit a, GameEntity t) throws GameRuleException {
        gameManager.executeAttack(a,t);
    }

    @Override public void nextTurn() {
        gameManager.advanceTurn();
    }

    @Override public String getStatus() {
        return "Game in Progress";
    }
}
