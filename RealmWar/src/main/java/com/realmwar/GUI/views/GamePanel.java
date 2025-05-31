package main.java.com.realmwar.GUI.views;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final BlockPanel[][] blockPanels;
    private final int rows = 10;
    private final int cols = 10;

    public GamePanel() {
        setLayout(new GridLayout(rows, cols, 0, 0)); // فاصله صفر بین خانه‌ها
        setBackground(new Color(101, 67, 33));
        blockPanels = new BlockPanel[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                blockPanels[row][col] = new BlockPanel();
                add(blockPanels[row][col]);
            }
        }
    }

    public BlockPanel getBlockPanel(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return blockPanels[row][col];
        }
        return null;
    }
}