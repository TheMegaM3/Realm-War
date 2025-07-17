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

    /**
     * Removes a player from the turn order.
     * Adjusts the current player index if necessary to ensure it remains valid.
     * @param player The player to remove.
     */
    public void removePlayer(Player player) {
        int index = players.indexOf(player);
        if (index != -1) {
            players.remove(index);
            // Adjust currentPlayerIndex if it points to or beyond the removed player
            if (currentPlayerIndex > index) {
                currentPlayerIndex--;
            } else if (currentPlayerIndex >= players.size() && !players.isEmpty()) {
                currentPlayerIndex = 0; // Reset to first player if out of bounds
            }
        }
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