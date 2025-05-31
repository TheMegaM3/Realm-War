package main.java.com.realmwar.GUI.views;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel() {
        setLayout(new FlowLayout());
        setBackground(Color.LIGHT_GRAY);

        JButton newGameBtn = new JButton("New Game");
        JButton loadGameBtn = new JButton("Load Game");
        JButton saveGameBtn = new JButton("Save Game");
        JButton exitBtn = new JButton("Exit");

        add(newGameBtn);
        add(loadGameBtn);
        add(saveGameBtn);
        add(exitBtn);
    }
}