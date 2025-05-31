package main.java.com.realmwar.GUI.views;

import javax.swing.*;
import java.awt.*;

public class ActionPanel extends JPanel {
    public ActionPanel() {
        setLayout(new FlowLayout());
        setBackground(Color.LIGHT_GRAY);

        JButton buildBtn = new JButton("Build");
        JButton trainBtn = new JButton("Train");
        JButton attackBtn = new JButton("Attack");
        JButton endTurnBtn = new JButton("End Turn");

        add(buildBtn);
        add(trainBtn);
        add(attackBtn);
        add(endTurnBtn);
    }
}