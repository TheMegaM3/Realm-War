package com.realmwar;

import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.view.GameFrame;
import javax.swing.SwingUtilities;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        SwingUtilities.invokeLater(() -> {

            GameManager gameManager = new GameManager(Arrays.asList("Player 1", "Player 2"), 16, 16);


            GameFrame gameFrame = new GameFrame(gameManager);




            gameFrame.setVisible(true);
        });
    }
}
