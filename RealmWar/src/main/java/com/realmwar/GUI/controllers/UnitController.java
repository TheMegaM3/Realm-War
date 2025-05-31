package main.java.com.realmwar.GUI.controllers;

import main.java.com.realmwar.GUI.views.GamePanel;

public class UnitController {
    private GamePanel gamePanel;

    public UnitController(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void moveUnit(int fromRow, int fromCol, int toRow, int toCol) {
        // Logic for moving units
        gamePanel.getBlockPanel(toRow, toCol).updateBlock("UNIT");
    }
}