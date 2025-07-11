package com.realmwar.engine;

import com.realmwar.model.Player;
import java.util.List;

/**
 * A simple class responsible for managing the turn order of players.
 */
public class TurnManager {
    private final List<Player> players;
    private int currentPlayerIndex;

    public TurnManager(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0; // The first player in the list starts.
    }

    /**
     * @return The Player object whose turn it currently is.
     */
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Advances the turn to the next player in the list, cycling back to the start.
     */
    public void nextTurn() {
        if (players.isEmpty()) {
            return;
        }
        // Modulo operator ensures the index wraps around to 0 after the last player.
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Sets the current player index directly. Used when loading a saved game.
     * @param index The new player index.
     */
    public void setCurrentPlayerIndex(int index) {
        if (index >= 0 && index < players.size()) {
            this.currentPlayerIndex = index;
        }
    }
}
