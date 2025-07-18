// GameOverState.java
// Represents the game state when the game has ended in the RealmWar game.
// Implements the GameState interface to disable most player actions and display the winner.

package com.realmwar.engine.gamestate;

import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

// Concrete game state class for when the game is over
public class GameOverState extends GameState {

    // Stores the winning player, if any
    private final Player winner;

    // Constructor to initialize with GameManager and the winning player
    public GameOverState(GameManager gm, Player winner) {
        super(gm);
        this.winner = winner;
    }

    // Helper method to throw an exception for disallowed actions in this state
    private void throwGameOverException() throws GameRuleException {
        throw new GameRuleException("The game is over!");
    }

    // Prevents unit movement in the game over state
    @Override
    public void moveUnit(Unit u, int x, int y) throws GameRuleException {
        throwGameOverException();
    }

    // Prevents unit attacks in the game over state
    @Override
    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException {
        throwGameOverException();
    }

    // Prevents advancing to the next turn in the game over state
    @Override
    public void nextTurn() {
        // Do nothing. The turn cannot advance when the game is over.
    }

    // Returns the game status, indicating the winner or a draw
    @Override
    public String getStatus() {
        return winner != null ? "GAME OVER! Winner: " + winner.getName() : "GAME OVER! It's a draw.";
    }
}