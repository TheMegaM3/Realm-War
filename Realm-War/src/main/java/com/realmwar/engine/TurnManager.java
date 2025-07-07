package com.realmwar.engine;

import com.realmwar.data.GameLogger;
import com.realmwar.model.Player;
import java.util.List;


public class TurnManager {


    private final List<Player> players;
    private int currentPlayerIndex;
    private final GameManager gameManager;
    private final TurnTimer turnTimer;

    public TurnManager(List<Player> players, GameManager gameManager) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.gameManager = gameManager;
        this.turnTimer = new TurnTimer(this::endCurrentTurn);
    }

    public void startTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        GameLogger.log("Turn started for player " + currentPlayer.getName());

        gameManager.setCurrentPlayer(currentPlayer);

        turnTimer.start();
    }

    public void endCurrentTurn() {
        turnTimer.stop();

        GameLogger.log("Turn ended for player: " + players.get(currentPlayerIndex).getName());

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        startTurn();
    }

    public TurnTimer getTurnTimer() {
        return turnTimer;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }


    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
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
