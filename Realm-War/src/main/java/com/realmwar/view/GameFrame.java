package com.realmwar.view;

import com.realmwar.Main;
import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.engine.gamestate.GameOverState;
import com.realmwar.engine.gamestate.RunningState;
import com.realmwar.model.GameEntity;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

// Main JFrame for the game UI
public class GameFrame extends JFrame {
    // Colors for buttons and background
    private static final Color BUTTON_RIGHT_COLOR = new Color(255, 183, 164); // Light coral for right buttons
    private static final Color BUTTON_LEFT_COLOR = new Color(125, 188, 182); // Teal for left buttons
    private static final Color BUTTON_TEXT_COLOR = new Color(54, 54, 54); // Dark gray for button text
    private static final Color BACKGROUND_COLOR = new Color(250, 240, 230); // Light cream background
    // Button dimensions and padding
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int HORIZONTAL_PADDING = 20;
    private static final int VERTICAL_PADDING = 15;

    // Game components
    private GameManager gameManager;
    private final GameBoardPanel gameBoardPanel; // Panel for rendering the game board
    private final InfoPanel infoPanel; // Panel for displaying player info
    private Timer turnTimer; // Timer for turn duration
    private Timer resourceTimer; // Timer for resource updates
    private int turnTimeLeft; // Remaining time in the current turn
    private boolean isMergeMode = false; // Flag for merge mode
    private Unit unitToMerge = null; // Unit selected for merging

    // Constructor to initialize the game window
    public GameFrame(GameManager gameManager) {
        this.gameManager = gameManager;
        setTitle("Realm War");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        loadAndSetIcon();

        // Main panel setup
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Info panel for player resources and turn info
        infoPanel = new InfoPanel(gameManager);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Center panel for buttons and game board
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        // Left buttons for game actions
        JPanel leftButtons = createButtonPanel(new String[]{"Build", "Train", "Upgrade", "Merge", "End Turn"}, BUTTON_LEFT_COLOR);
        setupLeftButtons(leftButtons);
        centerPanel.add(leftButtons, BorderLayout.WEST);

        // Game board panel
        gameBoardPanel = new GameBoardPanel(gameManager, this);
        centerPanel.add(gameBoardPanel, BorderLayout.CENTER);

        // Right buttons for game management
        JPanel rightButtons = createButtonPanel(new String[]{"New Game", "Load Game", "Save Game", "Exit"}, BUTTON_RIGHT_COLOR);
        setupRightButtons(rightButtons);
        centerPanel.add(rightButtons, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
        initializeTimers();
        resourceTimer.start();
        pack();
    }

    // Loads and sets the game window icon
    private void loadAndSetIcon() {
        try {
            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/game_icon.png")));
            setIconImage(icon);
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load game icon. Make sure game_icon.png is in src/assets.");
            e.printStackTrace();
        }
    }

    // Sets up action listeners for left buttons (game actions)
    private void setupLeftButtons(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton btn) {
                switch (btn.getText()) {
                    case "Build" -> btn.addActionListener(e -> showBuildDialog());
                    case "Train" -> btn.addActionListener(e -> showTrainDialog());
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

    // Sets up action listeners for right buttons (game management)
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

    // Stops all timers to prevent them from running in the background
    private void disposeTimers() {
        if (turnTimer != null) {
            turnTimer.stop();
        }
        if (resourceTimer != null) {
            resourceTimer.stop();
        }
    }

    // Handles starting a new game
    private void handleNewGame() {
        disposeTimers(); // Stop current timers before starting a new game
        this.dispose();
        Main.main(null);
    }

    // Handles loading a saved game
    private void handleLoadGame() {
        disposeTimers(); // Stop current timers before loading a new game
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
                GameFrame newFrame = new GameFrame(loadedGame);
                newFrame.setVisible(true);
                newFrame.updateView();
                newFrame.resetAndStartTurnTimer();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to load game. Starting a new game.", "Load Error", JOptionPane.ERROR_MESSAGE);
                Main.main(null);
            }
        } else {
            Main.main(null);
        }
    }

    // Handles saving the current game
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

    // Handles exiting the game
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to exit?", "Exit Game",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            disposeTimers(); // Stop timers before exiting
            System.exit(0);
        }
    }

    // Handles upgrading a structure
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

    // Activates merge mode
    private void handleMerge() {
        isMergeMode = true;
        unitToMerge = null;
        JOptionPane.showMessageDialog(this, "Merge mode activated. Select the first unit.", "Merge", JOptionPane.INFORMATION_MESSAGE);
    }

    // Shows dialog for building structures
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

    // Shows dialog for training units
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
                JOptionPane.showMessageDialog(this, unitType + " trained successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Train Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Checks if merge mode is active
    public boolean isMergeModeActive() {
        return isMergeMode;
    }

    // Handles unit merge selection
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

    // Creates a panel with styled buttons
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

        panel.remove(panel.getComponentCount() - 1); // Remove last strut
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // Creates a styled button
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

    // Updates the UI
    public void updateView() {
        if (gameManager == null) return;
        infoPanel.updateInfo(
                gameManager.getCurrentPlayer().getName(),
                gameManager.getCurrentPlayer().getResourceHandler().getGold(),
                gameManager.getCurrentPlayer().getResourceHandler().getFood()
        );
        gameBoardPanel.updatePanel(gameManager.getGameBoard(), null);
        if (gameManager.getCurrentState() instanceof GameOverState) {
            showGameOverDialog();
        }
    }

    // Shows game over dialog
    private void showGameOverDialog() {
        disposeTimers(); // Stop timers when game is over
        String winnerName = gameManager.winner != null ? gameManager.winner.getName() : "No one";
        String message = "Game Over! Winner: " + winnerName;

        Object[] options = {"New Game", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            Main.main(null);
        } else {
            System.exit(0);
        }
    }

    // Resets and starts the turn timer
    public void resetAndStartTurnTimer() {
        turnTimeLeft = Constants.TURN_DURATION_SECONDS;
        infoPanel.updateTimer(turnTimeLeft);
        if (turnTimer != null) {
            turnTimer.restart();
        }
    }

    // Initializes turn and resource timers
    private void initializeTimers() {
        turnTimer = new Timer(1000, e -> {
            turnTimeLeft--;
            infoPanel.updateTimer(turnTimeLeft);
            if (turnTimeLeft <= 0) {
                forceEndTurn();
            }
        });

        resourceTimer = new Timer(Constants.RESOURCE_TICK_MILLISECONDS, e -> {
            if (gameManager != null && gameManager.getCurrentState() instanceof RunningState) {
                gameManager.applyPeriodicResourceChanges();
                updateView();
            }
        });
    }

    // Forces the end of a turn when time runs out
    private void forceEndTurn() {
        turnTimer.stop();
        JOptionPane.showMessageDialog(this, "Time's up! Moving to the next player.", "Turn Ended", JOptionPane.INFORMATION_MESSAGE);
        gameManager.nextTurn();
        updateView();
        resetAndStartTurnTimer();
    }
}