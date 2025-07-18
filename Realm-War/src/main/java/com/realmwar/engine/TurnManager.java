// TurnManager.java
// Manages the turn order of players in the RealmWar game.
// Tracks the current player and handles turn progression and player removal.

package com.realmwar.engine;

import com.realmwar.model.Player;
import java.util.List;

// Class responsible for managing turn order
public class TurnManager {
    // List of players in the game
    private final List<Player> players;
    // Index of the current player in the turn order
    private int currentPlayerIndex;

    // Constructor to initialize with a list of players
    public TurnManager(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0; // First player starts
    }

    // Gets the current player whose turn it is
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    // Advances to the next player's turn, cycling back to the first player if necessary
    public void nextTurn() {
        if (players.isEmpty()) {
            return;
        }
        // Modulo ensures index wraps around to 0 after the last player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // Removes a player from the turn order and adjusts the current player index
    public void removePlayer(Player player) {
        int index = players.indexOf(player);
        if (index != -1) {
            players.remove(index);
            // Adjust index if it points to or beyond the removed player
            if (currentPlayerIndex > index) {
                currentPlayerIndex--;
            } else if (currentPlayerIndex >= players.size() && !players.isEmpty()) {
                currentPlayerIndex = 0; // Reset to first player if out of bounds
            }
        }
    }

    // Gets the current player index
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    // Sets the current player index, used for loading saved games
    public void setCurrentPlayerIndex(int index) {
        if (index >= 0 && index < players.size()) {
            this.currentPlayerIndex = index;
        }
    }
}