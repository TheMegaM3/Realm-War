package com.realmwar.view;

import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.engine.gamestate.RunningState;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class GameFrame extends JFrame {
    private static final Color BUTTON_RIGHT_COLOR = new Color(65, 105, 225);
    private static final Color BUTTON_LEFT_COLOR = new Color(147, 112, 219);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(101, 67, 33);
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BOARD_SIZE = 600;
    private static final int BUTTON_PANEL_WIDTH = 160;
    private static final int HORIZONTAL_PADDING = 20;
    private static final int VERTICAL_PADDING = 15;

    private GameManager gameManager;
    private final GameBoardPanel gameBoardPanel;
    private final InfoPanel infoPanel;


    public GameFrame(GameManager gameManager) {
        this.gameManager = gameManager;
        setTitle("Realm War");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);

        // پنل اصلی
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // پنل اطلاعات
        infoPanel = new InfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // پنل مرکزی
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        // دکمه‌های سمت چپ
        JPanel leftButtons = createButtonPanel(new String[]{"Build", "Train", "Attack", "End Turn"}, BUTTON_LEFT_COLOR);
        setupLeftButtons(leftButtons);
        centerPanel.add(leftButtons, BorderLayout.WEST);

        // صفحه بازی
        gameBoardPanel = new GameBoardPanel(gameManager);
        centerPanel.add(gameBoardPanel, BorderLayout.CENTER);

        // دکمه‌های سمت راست
        JPanel rightButtons = createButtonPanel(new String[]{"New Game", "Load Game", "Save Game", "Exit"}, BUTTON_RIGHT_COLOR);
        setupRightButtons(rightButtons);
        centerPanel.add(rightButtons, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
        pack();
    }

    // --- متدهای اختصاصی برای اتصال دکمه‌ها ---
    private void setupLeftButtons(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                switch (btn.getText()) {
                    case "Build":
                        btn.addActionListener(e -> showBuildDialog());
                        break;
                    case "Train":
                        btn.addActionListener(e -> showTrainDialog());
                        break;
                    case "Attack":
                        btn.addActionListener(e -> handleAttack());
                        break;
                    case "End Turn":
                        btn.addActionListener(e -> {
                            gameManager.nextTurn();
                            updateView();
                        });
                        break;
                }
            }
        }
    }

    private void setupRightButtons(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                switch (btn.getText()) {
                    case "New Game":
                        btn.addActionListener(e -> handleNewGame());
                        break;
                    case "Load Game":
                        btn.addActionListener(e -> handleLoadGame());
                        break;
                    case "Save Game":
                        btn.addActionListener(e -> handleSaveGame());
                        break;
                    case "Exit":
                        btn.addActionListener(e -> handleExit());
                        break;
                }
            }
        }
    }

    // متدهای مربوط به هر دکمه:
    private void handleNewGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Start a new game? Current progress will be lost.",
                "New Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            GameManager newGame = new GameManager(
                    Arrays.asList("Player 1", "Player 2"),
                    Constants.DEFAULT_BOARD_WIDTH,
                    Constants.DEFAULT_BOARD_HEIGHT
            );
            this.gameManager = newGame;
            this.gameBoardPanel.updatePanel(newGame.getGameBoard(), null);
            updateView();
            JOptionPane.showMessageDialog(this, "New game started!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleLoadGame() {
        String[] saveFiles = DatabaseManager.getSaveGames(); // فرض کنید این متد در DatabaseManager وجود دارد
        if (saveFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No saved games found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedSave = (String) JOptionPane.showInputDialog(
                this,
                "Select a game to load:",
                "Load Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                saveFiles,
                saveFiles[0]
        );

        if (selectedSave != null) {
            GameManager loadedGame = DatabaseManager.loadGame(selectedSave);
            if (loadedGame != null) {
                this.gameManager = loadedGame;
                this.gameBoardPanel.updatePanel(loadedGame.getGameBoard(), null);
                updateView();
                JOptionPane.showMessageDialog(this, "Game loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleSaveGame() {
        // ایجاد دیالوگ برای دریافت نام ذخیره
        String saveName = (String) JOptionPane.showInputDialog(
                this,
                "Enter save name:",
                "Save Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "save_" + System.currentTimeMillis()
        );

        if (saveName != null && !saveName.trim().isEmpty()) {
            try {
                boolean success = DatabaseManager.saveGame(gameManager, saveName);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Game saved successfully as: " + saveName,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to save game!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving game: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // --- متدهای کمکی برای عملیات دکمه‌ها ---
    private void showBuildDialog() {
        int[] selectedTile = gameManager.getSelectedTile();
        if (selectedTile[0] < 0 || selectedTile[1] < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tile first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = {
                "Farm (🌾) - " + Constants.FARM_BUILD_COST + " Gold",
                "Barrack (🛡️) - " + Constants.BARRACK_BUILD_COST + " Gold",
                "Market (🏪) - " + Constants.MARKET_BUILD_COST + " Gold",
                "Tower (🏰) - " + Constants.TOWER_BUILD_COST + " Gold"
        };

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Select structure to build:",
                "Build Menu",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice != null) {
            try {
                String structureType = choice.split(" ")[0];
                gameManager.buildStructure(structureType, selectedTile[0], selectedTile[1]);
                updateView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showTrainDialog() {
        int[] selectedTile = gameManager.getSelectedTile();
        if (selectedTile[0] < 0 || selectedTile[1] < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tile first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ترتیب از قوی‌ترین به ضعیف‌ترین
        String[] options = {
                "Knight (⚔ 4) - " + Constants.KNIGHT_GOLD_COST + " Gold, " + Constants.KNIGHT_FOOD_COST + " Food",
                "Swordsman (⚔ 3) - " + Constants.SWORDSMAN_GOLD_COST + " Gold, " + Constants.SWORDSMAN_FOOD_COST + " Food",
                "Spearman (⚔ 2) - " + Constants.SPEARMAN_GOLD_COST + " Gold, " + Constants.SPEARMAN_FOOD_COST + " Food",
                "Peasant (⚔ 1) - " + Constants.PEASANT_GOLD_COST + " Gold, " + Constants.PEASANT_FOOD_COST + " Food"
        };

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Train Unit (Strongest → Weakest):",
                "Train",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice != null) {
            try {
                String unitType = choice.split(" ")[0];
                gameManager.trainUnit(unitType, selectedTile[0], selectedTile[1]);
                updateView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAttack() {
        int[] selectedTile = gameManager.getSelectedTile();
        if (selectedTile[0] < 0 || selectedTile[1] < 0) {
            JOptionPane.showMessageDialog(this, "Please select your unit first", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        GameEntity selected = gameManager.getGameBoard().getTile(selectedTile[0], selectedTile[1]).getEntity();

        if (selected instanceof Unit && selected.getOwner() == gameManager.getCurrentPlayer()) {
            gameBoardPanel.setAttackingUnit((Unit)selected);
            JOptionPane.showMessageDialog(this, "Now select target to attack", "Attack", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Only your own units can attack", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Start a new game?",
                "New Game",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            // ریست بازی (باید در GameManager پیاده‌سازی شود)
            JOptionPane.showMessageDialog(this, "New game started.");
        }
    }

    private void loadGame() {
        String saveName = JOptionPane.showInputDialog(this, "Enter save name:");
        if (saveName != null && !saveName.trim().isEmpty()) {
            // منطق بارگذاری بازی (باید در DatabaseManager پیاده‌سازی شود)
            JOptionPane.showMessageDialog(this, "Loaded: " + saveName);
        }
    }

    private void saveGame() {
        String saveName = JOptionPane.showInputDialog(this, "Enter save name:");
        if (saveName != null && !saveName.trim().isEmpty()) {
            // منطق ذخیره بازی (باید در DatabaseManager پیاده‌سازی شود)
            JOptionPane.showMessageDialog(this, "Saved as: " + saveName);
        }
    }

    private JPanel createButtonPanel(String[] buttonTexts, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, HORIZONTAL_PADDING, 0, HORIZONTAL_PADDING));

        // اضافه کردن فضای خالی برای تراز وسط
        panel.add(Box.createVerticalGlue());

        for (String text : buttonTexts) {
            JButton btn = createStyledButton(text, bgColor);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(btn);
            panel.add(Box.createVerticalStrut(VERTICAL_PADDING));
        }

        // حذف فاصله اضافی بعد از آخرین دکمه
        panel.remove(panel.getComponentCount() - 1);

        // اضافه کردن فضای خالی برای تراز وسط
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
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

    public void updateView() {
        infoPanel.updateInfo(
                gameManager.getCurrentPlayer().getName(),
                gameManager.getCurrentPlayer().getResourceHandler().getGold(),
                gameManager.getCurrentPlayer().getResourceHandler().getFood()
        );
        gameBoardPanel.updatePanel(gameManager.getGameBoard(), null);

    }
}