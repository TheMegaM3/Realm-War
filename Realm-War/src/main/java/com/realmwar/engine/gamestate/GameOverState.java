package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

/**
 * Represents the state where the game has ended.
 * In this state, most player actions are disabled.
 */
public class GameOverState extends GameState {

    private final Player winner;

    public GameOverState(GameManager gm, Player winner) {
        super(gm);
        this.winner = winner;
    }

    /**
     * A helper method to throw an exception, preventing actions in this state.
     */
    private void throwGameOverException() throws GameRuleException {
        throw new GameRuleException("The game is over!");
    }

    @Override
    public void moveUnit(Unit u, int x, int y) throws GameRuleException {
        throwGameOverException();
    }

    @Override
    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException {
        throwGameOverException();
    }

    @Override
    public void nextTurn() {
        // Do nothing. The turn cannot advance when the game is over.
    }

    @Override
    public String getStatus() {
        return winner != null ? "GAME OVER! Winner: " + winner.getName() : "GAME OVER! It's a draw.";
    }
}