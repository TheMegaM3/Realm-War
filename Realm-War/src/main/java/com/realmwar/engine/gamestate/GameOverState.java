package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

public class GameOverState extends GameState {

    private final Player winner;
    public GameOverState(GameManager gm, Player w) {
        super(gm); this.winner=w;
    }
    private void ex() throws GameRuleException {
        throw new GameRuleException("The game is over!");
    }
    @Override public void moveUnit(Unit u, int x, int y) throws GameRuleException {
        ex();
    }

    @Override public void attackUnit(Unit a, GameEntity t) throws GameRuleException {
        ex();
    }
    @Override public void nextTurn() {}
    @Override public String getStatus() {
        return winner!=null ? "GAME OVER! Winner: "+winner.getName() : "GAME OVER! It's a draw."; }
}
