package main.java.com.realmwar.GUI.controllers;

import main.java.com.realmwar.GUI.views.GamePanel;

public class StructureController {
    private GamePanel gamePanel;

    public StructureController(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void buildStructure(int row, int col, String type) {
        // Logic for building structures
        gamePanel.getBlockPanel(row, col).updateBlock(type);
    }
}