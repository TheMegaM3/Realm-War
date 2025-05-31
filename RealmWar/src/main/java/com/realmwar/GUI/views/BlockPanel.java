package main.java.com.realmwar.GUI.views;

import javax.swing.*;
import java.awt.*;

public class BlockPanel extends JPanel {
    private static final int SIZE = 60;
    private boolean isGreenHouse = false;

    public BlockPanel() {
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMinimumSize(new Dimension(SIZE, SIZE));
        setMaximumSize(new Dimension(SIZE, SIZE));
        setBackground(new Color(210, 180, 140));
        setNormalBorder();
        setOpaque(true);
        setLayout(new GridBagLayout());
    }

    public void setAsGreenHouse() {
        this.isGreenHouse = true;
        setBackground(new Color(34, 139, 34));
        repaint();
    }

    private void setNormalBorder() {
        // اصلاح شده: اضافه کردن پارامتر thickness (ضخامت) به متد createLineBorder
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // حاشیه بیرونی
                BorderFactory.createLineBorder(new Color(150, 120, 90), 1)  // حاشیه داخلی
        ));
    }
}