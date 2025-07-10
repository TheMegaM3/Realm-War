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
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        // بازنشانی وضعیت تمام واحدهای بازیکن جدید از طریق GameBoard
        // این خط را حذف کنید:
        // players.get(currentPlayerIndex).getUnits().forEach(u -> u.setHasActedThisTurn(false));

        // به جای آن از GameBoard برای مدیریت واحدها استفاده می‌شود
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