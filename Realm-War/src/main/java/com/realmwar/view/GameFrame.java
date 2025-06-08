package com.realmwar.view;

import com.realmwar.data.DatabaseManager;
import com.realmwar.engine.GameManager;
import com.realmwar.model.GameEntity;
import com.realmwar.model.structures.Barrack;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.structures.TownHall;
import com.realmwar.model.units.Peasant;
import com.realmwar.model.units.Unit;
import com.realmwar.util.CustomExceptions.GameRuleException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The main application window. This FINAL version includes the controller logic
 * for contextual menus for building and training.
 */
public class GameFrame extends JFrame {

    private final GameManager gameManager;
    private final GameBoardPanel gameBoardPanel;
    private final InfoPanel infoPanel;
    private GameEntity selectedEntity = null;

    public GameFrame(GameManager gameManager) {
        this.gameManager = gameManager;
        setTitle("Realm War - Final Interactive Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 700));

        infoPanel = new InfoPanel();
        gameBoardPanel = new GameBoardPanel();
        add(infoPanel, BorderLayout.NORTH);
        add(gameBoardPanel, BorderLayout.CENTER);

        initController();
        pack();
        setLocationRelativeTo(null);
    }

    private void initController() {
        infoPanel.nextTurnButton.addActionListener(e -> handleNextTurn());
        gameBoardPanel.addMouseListener(new GameBoardMouseListener());
        updateView();
    }

    private void handleNextTurn() {
        this.selectedEntity = null;
        gameManager.nextTurn();
        updateView();
    }

    private void handleTileClick(int x, int y) {
        GameEntity targetEntity = gameManager.getGameBoard().getTile(x, y).getEntity();

        // If a unit is selected, handle its action
        if (selectedEntity instanceof Unit) {
            handleUnitAction((Unit) selectedEntity, x, y, targetEntity);
        } else { // If nothing is selected, handle a new selection
            if (targetEntity != null && targetEntity.getOwner() == gameManager.getCurrentPlayer()) {
                selectedEntity = targetEntity;
                // If the newly selected entity is a building, show its menu
                if (selectedEntity instanceof Structure) {
                    showProductionMenu((Structure) selectedEntity);
                }
            } else {
                selectedEntity = null; // Deselect if clicking empty space or enemy
            }
        }
        updateView();
    }

    private void handleUnitAction(Unit selectedUnit, int x, int y, GameEntity targetEntity) {
        try {
            // If the selected unit is a Peasant and clicks an empty tile, show build menu
            if (selectedUnit instanceof Peasant && targetEntity == null) {
                showBuildMenu(x, y);
            } else { // Otherwise, it's a standard move or attack
                if (targetEntity != null) {
                    gameManager.attackUnit(selectedUnit, targetEntity);
                } else {
                    gameManager.moveUnit(selectedUnit, x, y);
                }
            }
        } catch (GameRuleException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Action Failed", JOptionPane.WARNING_MESSAGE);
        }
        selectedEntity = null; // Deselect after any action
    }

    private void showProductionMenu(Structure building) {
        String[] options;
        String title;

        if (building instanceof TownHall) {
            options = new String[]{"Train Peasant"};
            title = "TownHall Actions";
        } else if (building instanceof Barrack) {
            options = new String[]{"Train Spearman", "Train Swordsman"};
            title = "Barrack Actions";
        } else {
            return; // Not a production building
        }

        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice != -1) {
            String action = options[choice];
            // Here you would call a method on GameManager to execute the action, e.g.:
            // gameManager.trainUnit(building, action);
            JOptionPane.showMessageDialog(this, "Training: " + action);
        }
        selectedEntity = null; // Deselect after choosing
    }

    private void showBuildMenu(int x, int y) {
        String[] options = {"Build Farm", "Build Barracks"};
        String title = "Build Structure";

        int choice = JOptionPane.showOptionDialog(this, "Choose a structure to build at (" + x + "," + y + "):", title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice != -1) {
            String structureToBuild = options[choice].split(" ")[1]; // "Farm" or "Barracks"
            // Here you would call a method on GameManager to build, e.g.:
            // gameManager.buildStructure((Peasant)selectedEntity, structureToBuild, x, y);
            JOptionPane.showMessageDialog(this, "Building: " + structureToBuild);
        }
    }

    private void updateView() {
        infoPanel.updateInfo(gameManager.getCurrentPlayer(), gameManager.getCurrentState().getStatus());
        gameBoardPanel.updatePanel(gameManager.getGameBoard(), selectedEntity);
    }

    private class GameBoardMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int tileWidth = gameBoardPanel.getWidth() / gameManager.getGameBoard().width;
            int tileHeight = gameBoardPanel.getHeight() / gameManager.getGameBoard().height;
            int clickedX = e.getX() / tileWidth;
            int clickedY = e.getY() / tileHeight;
            handleTileClick(clickedX, clickedY);
        }
    }
    // --- getters ---

    public GameBoardPanel getGameBoardPanel() {
        return gameBoardPanel;
    }

    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    public GameEntity getSelectedEntity() {
        return selectedEntity;
    }

    public GameManager getGameManager() {
        return gameManager;
    }



}
