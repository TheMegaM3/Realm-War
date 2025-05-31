package main.java.com.realmwar.GUI.controllers;

import main.java.com.realmwar.GUI.views.InfoPanel;

public class PlayerController {
    private InfoPanel infoPanel;

    public PlayerController(InfoPanel infoPanel) {
        this.infoPanel = infoPanel;
    }

    public void updatePlayerStats(String name, int gold, int food) {
        infoPanel.updatePlayerInfo(name, gold, food);
    }
}