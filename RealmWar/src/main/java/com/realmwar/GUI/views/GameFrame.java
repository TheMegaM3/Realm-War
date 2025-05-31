package main.java.com.realmwar.GUI.views;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    // رنگ‌ها
    private static final Color BUTTON_RIGHT_COLOR = new Color(65, 105, 225);
    private static final Color BUTTON_LEFT_COLOR = new Color(147, 112, 219);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(101, 67, 33);
    private static final Color INFO_PANEL_COLOR = new Color(70, 50, 30);

    // اندازه‌ها و فاصله‌ها
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final int PREF_WIDTH = 1000;
    private static final int PREF_HEIGHT = 700;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 45;
    private static final int VERTICAL_PADDING = 20;
    private static final int INFO_PADDING = 15;

    private final GamePanel gamePanel;
    private final JPanel rightButtonPanel;
    private final JPanel leftButtonPanel;
    private final JPanel infoPanel;

    public GameFrame() {
        // تنظیمات اصلی پنجره
        setTitle("REALM WAR - Final Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // پنل اصلی با پس‌زمینه قهوه‌ای
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // پنل بازی با فاصله‌های قهوه‌ای
        this.gamePanel = new GamePanel();
        JPanel gameWrapper = new JPanel(new BorderLayout());
        gameWrapper.setBackground(BACKGROUND_COLOR);
        gameWrapper.setBorder(BorderFactory.createEmptyBorder(VERTICAL_PADDING, 0, VERTICAL_PADDING, 0));
        gameWrapper.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(gameWrapper, BorderLayout.CENTER);

        // پنل دکمه‌ها با تراز وسط کامل
        this.rightButtonPanel = createButtonPanel(true);
        this.leftButtonPanel = createButtonPanel(false);

        JPanel rightWrapper = new JPanel();
        rightWrapper.setLayout(new BoxLayout(rightWrapper, BoxLayout.Y_AXIS));
        rightWrapper.setBackground(BACKGROUND_COLOR);
        rightWrapper.add(Box.createVerticalGlue());
        rightWrapper.add(rightButtonPanel);
        rightWrapper.add(Box.createVerticalGlue());
        rightWrapper.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JPanel leftWrapper = new JPanel();
        leftWrapper.setLayout(new BoxLayout(leftWrapper, BoxLayout.Y_AXIS));
        leftWrapper.setBackground(BACKGROUND_COLOR);
        leftWrapper.add(Box.createVerticalGlue());
        leftWrapper.add(leftButtonPanel);
        leftWrapper.add(Box.createVerticalGlue());
        leftWrapper.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        mainPanel.add(rightWrapper, BorderLayout.EAST);
        mainPanel.add(leftWrapper, BorderLayout.WEST);

        // پنل اطلاعات
        this.infoPanel = createInfoPanel();
        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setBackground(BACKGROUND_COLOR);
        infoWrapper.setBorder(BorderFactory.createEmptyBorder(INFO_PADDING, 0, 0, 0));
        infoWrapper.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(infoWrapper, BorderLayout.NORTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setGreenHouse(3, 4);
    }

    private JPanel createButtonPanel(boolean isRightPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);

        String[] buttons = isRightPanel ?
                new String[]{"New Game", "Load Game", "Save Game", "Exit"} :
                new String[]{"Build", "Train", "Attack", "End Turn"};

        for (String btnText : buttons) {
            JButton btn = createStyledButton(btnText,
                    isRightPanel ? BUTTON_RIGHT_COLOR : BUTTON_LEFT_COLOR);
            panel.add(btn);
            panel.add(Box.createVerticalStrut(15));
        }

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        btn.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        btn.setBackground(bgColor);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return btn;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        panel.setBackground(INFO_PANEL_COLOR);

        JLabel playerLabel = createInfoLabel("Player: Test Player", Color.WHITE);
        JLabel goldLabel = createInfoLabel("Gold: 100", Color.YELLOW);
        JLabel foodLabel = createInfoLabel("Food: 75", new Color(144, 238, 144));

        panel.add(playerLabel);
        panel.add(goldLabel);
        panel.add(foodLabel);

        return panel;
    }

    private JLabel createInfoLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private void setGreenHouse(int row, int col) {
        BlockPanel panel = gamePanel.getBlockPanel(row, col);
        if (panel != null) {
            panel.setAsGreenHouse();
        }
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}