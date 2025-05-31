package main.java.com.realmwar.GUI.views;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {
    private JLabel playerNameLabel;
    private JLabel goldLabel;
    private JLabel foodLabel;

    public InfoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.LIGHT_GRAY);
        setPreferredSize(new Dimension(200, 0));

        playerNameLabel = new JLabel("Player: ");
        goldLabel = new JLabel("Gold: 0");
        foodLabel = new JLabel("Food: 0");

        add(playerNameLabel);
        add(goldLabel);
        add(foodLabel);
    }

    public void updatePlayerInfo(String name, int gold, int food) {
        playerNameLabel.setText("Player: " + name);
        goldLabel.setText("Gold: " + gold);
        foodLabel.setText("Food: " + food);
    }
}