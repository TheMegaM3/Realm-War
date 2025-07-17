package com.realmwar.engine;

import com.realmwar.model.Player;
import java.util.List;

public class TurnManager {
    private final List<Player> players;
    private int currentPlayerIndex;

    public TurnManager(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0;
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if (players.isEmpty()) {
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public void removePlayer(Player player) {
        int index = players.indexOf(player);
        if (index != -1) {
            players.remove(index);
            if (currentPlayerIndex >= index && !players.isEmpty()) {
                currentPlayerIndex = currentPlayerIndex % players.size();
            }
        }
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int index) {
        if (index >= 0 && index < players.size()) {
            this.currentPlayerIndex = index;
        }
    }
}
