package com.realmwar.view;

import com.realmwar.Main;
import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.engine.gamestate.RunningState;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * The main window (JFrame) of the application.
 * It holds all the UI panels (InfoPanel, GameBoardPanel, button panels)
 * and manages the game timers.
 */
public class GameFrame extends JFrame {
    // --- UI Styling Constants ---
    private static final Color BUTTON_RIGHT_COLOR = new Color(255, 183, 164);
    private static final Color BUTTON_LEFT_COLOR = new Color(125, 188, 182);
    private static final Color BUTTON_TEXT_COLOR = new Color(54, 54, 54);
    private static final Color BACKGROUND_COLOR = new Color(250, 240, 230);
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int HORIZONTAL_PADDING = 20;
    private static final int VERTICAL_PADDING = 15;

    // --- Game State References ---
    private GameManager gameManager;
    private final GameBoardPanel gameBoardPanel;
    private final InfoPanel infoPanel;

    // --- Timers and Turn Management ---
    private Timer turnTimer;
    private Timer resourceTimer;
    private int turnTimeLeft;

    // --- UI State Flags ---
    private boolean isMergeMode = false;
    private Unit unitToMerge = null;

    public GameFrame(GameManager gameManager) {
        this.gameManager = gameManager;

        // --- Frame Setup ---
        setTitle("Realm War");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null); // Center the window on screen.
        loadAndSetIcon(); // Set the application icon.

        // --- Panel Assembly ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));


        // Info Panel
        infoPanel = new InfoPanel(gameManager);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        // Left Buttons
        JPanel leftButtons = createButtonPanel(new String[]{"Build", "Train", "Attack", "Upgrade", "Merge", "End Turn"}, BUTTON_LEFT_COLOR);
        setupLeftButtons(leftButtons);
        centerPanel.add(leftButtons, BorderLayout.WEST);

        // Game Board
        gameBoardPanel = new GameBoardPanel(gameManager, this);
        centerPanel.add(gameBoardPanel, BorderLayout.CENTER);

        // Right Buttons
        JPanel rightButtons = createButtonPanel(new String[]{"New Game", "Load Game", "Save Game", "Exit"}, BUTTON_RIGHT_COLOR);
        setupRightButtons(rightButtons);
        centerPanel.add(rightButtons, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
        initializeTimers();
        resourceTimer.start();
        pack(); // Adjust window size to fit all components.
    }

    /**
     * Loads the application icon from the assets folder and sets it for the JFrame.
     */
    private void loadAndSetIcon() {
        try {

            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/game_icon.png")));
            setIconImage(icon);
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load game icon. Make sure game_icon.png is in src/assets.");
            e.printStackTrace();
        }
    }

    /**
     * Attaches action listeners to the left-side (game action) buttons.
     */
    private void setupLeftButtons(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton btn) {
                switch (btn.getText()) {
                    case "Build" -> btn.addActionListener(e -> showBuildDialog());
                    case "Train" -> btn.addActionListener(e -> showTrainDialog());
                    case "Attack" -> btn.addActionListener(e -> handleAttack());
                    case "Upgrade" -> btn.addActionListener(e -> handleUpgrade());
                    case "Merge" -> btn.addActionListener(e -> handleMerge());
                    case "End Turn" -> btn.addActionListener(e -> {
                        gameManager.nextTurn();
                        updateView();
                        resetAndStartTurnTimer();
                    });
                }
            }
        }
    }

    /**
     * Attaches action listeners to the right-side (system) buttons.
     */
    private void setupRightButtons(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton btn) {
                switch (btn.getText()) {
                    case "New Game" -> btn.addActionListener(e -> handleNewGame());
                    case "Load Game" -> btn.addActionListener(e -> handleLoadGame());
                    case "Save Game" -> btn.addActionListener(e -> handleSaveGame());
                    case "Exit" -> btn.addActionListener(e -> handleExit());
                }
            }
        }
    }

    // --- Button Handler Methods ---

    /**
     * Handles the "New Game" action by disposing the current window and restarting the application.
     * This is the most robust way to ensure a clean state.
     */
    private void handleNewGame() {
        this.dispose();
        Main.main(null);
    }

    /**
     * Handles the "Load Game" action by closing the current window and creating a new one
     * with the loaded game state. This prevents stale references and bugs.
     */
    private void handleLoadGame() {
        // First, dispose the current frame so it doesn't interfere.
        this.dispose();

        String[] saveFiles = DatabaseManager.getSaveGames();
        if (saveFiles.length == 0) {
            JOptionPane.showMessageDialog(null, "No saved games found! Starting a new game.", "Error", JOptionPane.ERROR_MESSAGE);
            Main.main(null);
            return;
        }

        String selectedSave = (String) JOptionPane.showInputDialog(
                null, "Select a game to load:", "Load Game",
                JOptionPane.PLAIN_MESSAGE, null, saveFiles, saveFiles[0]);

        if (selectedSave != null) {
            GameManager loadedGame = DatabaseManager.loadGame(selectedSave);
            if (loadedGame != null) {
                // Create a completely new GameFrame with the loaded game state
                GameFrame newFrame = new GameFrame(loadedGame);
                newFrame.setVisible(true);
                newFrame.updateView();
                newFrame.resetAndStartTurnTimer();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to load game. Starting a new game.", "Load Error", JOptionPane.ERROR_MESSAGE);
                Main.main(null);
            }
        } else {
            // If user cancels the load dialog, restart the main application flow
            Main.main(null);
        }
    }

    /**
     * Handles the "Save Game" action.
     */
    private void handleSaveGame() {
        String saveName = (String) JOptionPane.showInputDialog(
                this, "Enter save name:", "Save Game",
                JOptionPane.PLAIN_MESSAGE, null, null, "save_" + System.currentTimeMillis());

        if (saveName != null && !saveName.trim().isEmpty()) {
            boolean success = DatabaseManager.saveGame(gameManager, saveName);
            if (success) {
                JOptionPane.showMessageDialog(this, "Game saved successfully as: " + saveName, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save game!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the "Exit" action.
     */
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to exit?", "Exit Game",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    private void handleUpgrade() {
        int[] selectedTile = gameManager.getSelectedTile();
        if (selectedTile[0] < 0) {
            JOptionPane.showMessageDialog(this, "Please select a structure to upgrade.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            gameManager.upgradeStructure(selectedTile[0], selectedTile[1]);
            updateView();
            JOptionPane.showMessageDialog(this, "Structure upgraded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Upgrade Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMerge() {
        isMergeMode = true;
        unitToMerge = null;
        JOptionPane.showMessageDialog(this, "Merge mode activated. Select the first unit.", "Merge", JOptionPane.INFORMATION_MESSAGE);
    }

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

    public boolean isMergeModeActive() {
        return isMergeMode;
    }

    public void handleMergeClick(Unit clickedUnit) {
        if (unitToMerge == null) {
            unitToMerge = clickedUnit;
            JOptionPane.showMessageDialog(this, "First unit selected. Now select an adjacent, identical unit to merge with.", "Merge", JOptionPane.INFORMATION_MESSAGE);
        } else {
            try {
                gameManager.mergeUnits(unitToMerge, clickedUnit);
                isMergeMode = false;
                unitToMerge = null;
                updateView();
                JOptionPane.showMessageDialog(this, "Merge successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Merge Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                isMergeMode = false;
                unitToMerge = null;
            }
        }
    }

    private JPanel createButtonPanel(String[] buttonTexts, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, HORIZONTAL_PADDING, 0, HORIZONTAL_PADDING));
        panel.add(Box.createVerticalGlue());

        for (String text : buttonTexts) {
            JButton btn = createStyledButton(text, bgColor);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(btn);
            panel.add(Box.createVerticalStrut(VERTICAL_PADDING));
        }

        panel.remove(panel.getComponentCount() - 1);
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

    /**
     * A central method to refresh the entire UI based on the game manager's state.
     */
    public void updateView() {
        if (gameManager == null) return;
        infoPanel.updateInfo(
                gameManager.getCurrentPlayer().getName(),
                gameManager.getCurrentPlayer().getResourceHandler().getGold(),
                gameManager.getCurrentPlayer().getResourceHandler().getFood()
        );
        gameBoardPanel.updatePanel(gameManager.getGameBoard(), null); // Pass a fresh board reference
    }

    /**
     * Initializes the game timers for turn duration and periodic resource generation.
     */
    private void initializeTimers() {
        // This timer counts down the seconds for the current player's turn.
        turnTimer = new Timer(1000, e -> {
            turnTimeLeft--;
            infoPanel.updateTimer(turnTimeLeft);
            if (turnTimeLeft <= 0) {
                forceEndTurn();
            }
        });

        // This timer periodically calls the resource generation logic in GameManager.
        resourceTimer = new Timer(Constants.RESOURCE_TICK_MILLISECONDS, e -> {
            if (gameManager != null && gameManager.getCurrentState() instanceof RunningState) {
                //  The ONLY call here should be to the GameManager.
                gameManager.applyPeriodicResourceChanges();
                updateView(); // Update the UI to show new resource totals.
            }
        });
    }

    /**
     * Resets and restarts the turn timer. Called at the beginning of each new turn.
     */
    public void resetAndStartTurnTimer() {
        turnTimeLeft = Constants.TURN_DURATION_SECONDS;
        infoPanel.updateTimer(turnTimeLeft);
        if(turnTimer != null) {
            turnTimer.restart();
        }
    }

    /**
     * Called by the timer when a player's time runs out.
     */
    private void forceEndTurn() {
        turnTimer.stop();
        JOptionPane.showMessageDialog(this, "Time's up! Moving to the next player.", "Turn Ended", JOptionPane.INFORMATION_MESSAGE);
        gameManager.nextTurn();
        updateView();
        resetAndStartTurnTimer();
    }
}
