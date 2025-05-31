package main.java.com.realmwar;

import main.java.com.realmwar.GUI.views.GameFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setLocationRelativeTo(null); // نمایش پنجره در مرکز صفحه
            frame.setVisible(true);
        });
    }
}