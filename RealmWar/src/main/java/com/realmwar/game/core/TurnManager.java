package main.java.com.realmwar.game.core;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.util.GameLogger;

import java.util.List;

public class TurnManager {
    private List<Player> players;
    private int currentPlayerIndex;

    public TurnManager(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0;
        GameLogger.log("TurnManager initialized with " + players.size() + " players.");
    }

    public void setPlayers(List<Player> players) {
        this.players = players;

        if (!players.isEmpty() && currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        GameLogger.log("TurnManager player list updated.");
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            GameLogger.logError("No players in TurnManager.");
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if (players.isEmpty()) {
            GameLogger.logWarning("Cannot advance turn: No players available.");
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        GameLogger.log("Turn advanced. Now it's " + getCurrentPlayer().getName() + "'s turn.");
    }

    public void startTurns() {
        if (!players.isEmpty()) {
            getCurrentPlayer().getResourceHandler().generateResources();
            GameLogger.log("First turn initiated for " + getCurrentPlayer().getName() + ".");
        }
    }
}
