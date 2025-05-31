package main.java.com.realmwar.GUI.controllers;

import main.java.com.realmwar.GUI.views.*;

public class GameController {
    private GameFrame gameFrame;
    private GamePanel gamePanel;

    public GameController() {
        gameFrame = new GameFrame();
        gamePanel = gameFrame.getGamePanel();
        initialize();
    }

    private void initialize() {
        // Initialize game logic and setup listeners
    }
}