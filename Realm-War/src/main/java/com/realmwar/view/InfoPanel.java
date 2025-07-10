package com.realmwar.view;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {
    private JLabel playerLabel;
    private JLabel goldLabel;
    private JLabel foodLabel;

    public InfoPanel() {
        // تنظیمات لایه‌بندی و ظاهر
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
        setBackground(new Color(70, 50, 30)); // رنگ قهوه‌ای تیره
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 0, 15, 0), // فاصله داخلی بالا و پایین
                BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(101, 67, 33)) // حاشیه نامرئی
        ));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 40)); // ارتفاع ثابت

        // ایجاد و تنظیم لیبل‌ها
        playerLabel = new JLabel("Player: Player1");
        goldLabel = new JLabel("Gold: 500");
        foodLabel = new JLabel("Food: 200");

        // تنظیم فونت
        Font boldFont = new Font("Arial", Font.BOLD, 14);
        playerLabel.setFont(boldFont);
        goldLabel.setFont(boldFont);
        foodLabel.setFont(boldFont);

        // تنظیم رنگ متن
        playerLabel.setForeground(new Color(0, 255, 255));
        goldLabel.setForeground(new Color(255, 215, 0)); // طلایی روشن
        foodLabel.setForeground(new Color(255, 174, 201)); // صورتی روشن

        // تنظیم سایه برای متن‌ها
        playerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        goldLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        foodLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));

        // اضافه کردن کامپوننت‌ها
        add(playerLabel);
        add(Box.createHorizontalStrut(10));
        add(goldLabel);
        add(Box.createHorizontalStrut(10));
        add(foodLabel);
    }

    public void updateInfo(String playerName, int gold, int food) {
        playerLabel.setText("Player: " + playerName);
        goldLabel.setText("Gold: " + gold);
        foodLabel.setText("Food: " + food);

        // برای اطمینان از بروزرسانی صحیح نمایش
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // ایجاد افکت گرادیانت برای زیبایی
        Graphics2D g2d = (Graphics2D) g;
        Color startColor = new Color(70, 50, 30);
        Color endColor = new Color(90, 70, 50);
        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}