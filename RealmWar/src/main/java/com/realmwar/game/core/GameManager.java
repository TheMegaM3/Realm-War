package main.java.com.realmwar.game.core;

import main.java.com.realmwar.game.entities.Player;
import main.java.com.realmwar.game.entities.blocks.Block;
import main.java.com.realmwar.game.entities.blocks.EmptyBlock;
import main.java.com.realmwar.game.entities.blocks.ForestBlock;
import main.java.com.realmwar.game.entities.blocks.VoidBlock;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.structures.TownHall;
import main.java.com.realmwar.game.entities.units.Unit;
import main.java.com.realmwar.game.util.CustomExceptions;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameManager {
    private static GameManager instance;
    private List<Player> players;
    private TurnManager turnManager;
    private Block[][] gameBoard;
    private int boardRows;
    private int boardCols;

    private GameManager(int rows, int cols) {
        this.boardRows = rows;
        this.boardCols = cols;
        this.gameBoard = new Block[rows][cols];
        this.players = new ArrayList<>();
        this.turnManager = new TurnManager(players);
        GameLogger.log("GameManager initialized.");
    }

    public static GameManager getInstance(int rows, int cols) {
        if (instance == null) {
            instance = new GameManager(rows, cols);
        }
        return instance;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameManager has not been initialized yet. Call getInstance(rows, cols) first.");
        }
        return instance;
    }


    public void addPlayer(Player player) {
        this.players.add(player);
        GameLogger.log("Player added: " + player.getName());
        turnManager.setPlayers(players);
    }

    public void startGame() {
        if (players.isEmpty()) {
            GameLogger.logError("Cannot start game: No players added.");
            return;
        }
        initializeGameBoard();
        turnManager.startTurns();
        GameLogger.log("Game started! Current player: " + turnManager.getCurrentPlayer().getName());
        // The User Interface (UI) should be updated after this call.
    }

    private void initializeGameBoard() {
        Random random = new Random();
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                if (random.nextDouble() < Constants.FOREST_BLOCK_CHANCE) {
                    gameBoard[r][c] = new ForestBlock(r, c);
                } else {
                    gameBoard[r][c] = new EmptyBlock(r, c);
                }
            }
        }

        if (!players.isEmpty()) {
            Player p1 = players.get(0);
            TownHall th1 = new TownHall(p1, 0, 0);
            gameBoard[0][0].setStructure(th1);
            GameLogger.log("TownHall placed for " + p1.getName() + " at (0,0).");

            if (players.size() > 1) {
                Player p2 = players.get(1);
                TownHall th2 = new TownHall(p2, boardRows - 1, boardCols - 1);
                gameBoard[boardRows - 1][boardCols - 1].setStructure(th2);
                GameLogger.log("TownHall placed for " + p2.getName() + " at (" + (boardRows - 1) + "," + (boardCols - 1) + ").");
            }
        }
        GameLogger.log("Game board initialized with blocks and starting TownHalls.");
    }

    public Player getCurrentPlayer() {
        return turnManager.getCurrentPlayer();
    }


    public void endCurrentPlayerTurn() {
        GameLogger.log("Player " + turnManager.getCurrentPlayer().getName() + " ended turn.");

        for (Structure structure : getCurrentPlayer().getStructures()) {
            if (structure instanceof com.realmwar.game.entities.structures.Tower) {
                ((com.realmwar.game.entities.structures.Tower) structure).defend();
            }
        }

        turnManager.nextTurn();
        Player newPlayer = turnManager.getCurrentPlayer();
        newPlayer.getResourceHandler().generateResources();
        GameLogger.log("New current player: " + newPlayer.getName());

        if (checkWinCondition()) {
            GameLogger.log("Game Over!");

        }
    }

    public boolean checkWinCondition() {
        List<Player> activePlayers = players.stream()
                .filter(Player::hasTownHall)
                .collect(Collectors.toList());

        if (activePlayers.size() <= 1 && players.size() > 1) {
            if (activePlayers.size() == 1) {
                GameLogger.log("Player " + activePlayers.get(0).getName() + " wins the game!");
            } else {
                GameLogger.log("No players left with Town Hall. It's a draw or game over.");
            }
            return true;
        } else if (players.size() == 1 && !players.get(0).hasTownHall()) {
            GameLogger.log("Single player lost their Town Hall. Game Over.");
            return true;
        }
        return false;
    }


    public Block getBlockAt(int row, int col) {
        if (row < 0 || row >= boardRows || col < 0 || col >= boardCols) {
            return new VoidBlock(row, col);
        }
        return gameBoard[row][col];
    }


    public void placeStructure(Player player, Structure structure, int row, int col) throws CustomExceptions.InsufficientResourcesException, CustomExceptions.InvalidPlacementException {
        if (player != getCurrentPlayer()) {
            throw new CustomExceptions.InvalidPlacementException("It's not your turn!");
        }
        if (row < 0 || row >= boardRows || col < 0 || col >= boardCols) {
            throw new CustomExceptions.InvalidPlacementException("Invalid board coordinates.");
        }

        Block targetBlock = gameBoard[row][col];


        if (!targetBlock.allowsBuilding()) {
            throw new CustomExceptions.InvalidPlacementException("Cannot build on this type of block (e.g., VoidBlock).");
        }
        if (targetBlock.getStructure() != null) {
            throw new CustomExceptions.InvalidPlacementException("Block already has a structure.");
        }


        if (targetBlock instanceof ForestBlock) {
            gameBoard[row][col] = new EmptyBlock(row, col);
            GameLogger.log("ForestBlock at (" + row + "," + col + ") converted to EmptyBlock.");
            targetBlock = gameBoard[row][col];
        }


        if (!player.getResourceHandler().canAfford(structure.getGoldCost(), structure.getFoodCost())) {
            throw new CustomExceptions.InsufficientResourcesException("Not enough resources to build " + structure.getClass().getSimpleName() + ".");
        }


        try {
            player.getResourceHandler().deductResources(structure.getGoldCost(), structure.getFoodCost());
        } catch (CustomExceptions.InsufficientResourcesException e) {

            throw new CustomExceptions.InsufficientResourcesException("Error deducting resources: " + e.getMessage());
        }

        structure.setPosition(row, col);
        targetBlock.setStructure(structure);
        player.addStructure(structure);

        GameLogger.log(player.getName() + " built a " + structure.getClass().getSimpleName() + " at (" + row + ", " + col + ").");
        // The UI should be updated after this call.
    }

    public void trainUnit(Player player, Unit unit) throws CustomExceptions.InsufficientResourcesException, CustomExceptions.InsufficientSpaceException, CustomExceptions.InvalidActionException {
        if (player != getCurrentPlayer()) {
            throw new CustomExceptions.InvalidActionException("It's not your turn to train units!");
        }
        if (!player.getResourceHandler().canAfford(unit.getGoldCost(), unit.getFoodCost())) {
            throw new CustomExceptions.InsufficientResourcesException("Not enough resources to train " + unit.getClass().getSimpleName() + ".");
        }
        if (!player.canAddUnit()) {
            throw new CustomExceptions.InsufficientSpaceException("Insufficient unit space.");
        }

        try {
            player.getResourceHandler().deductResources(unit.getGoldCost(), unit.getFoodCost());
        } catch (CustomExceptions.InsufficientResourcesException e) {
            throw new CustomExceptions.InsufficientResourcesException("Error deducting resources: " + e.getMessage());
        }

        player.addUnit(unit);
        GameLogger.log(player.getName() + " trained a " + unit.getClass().getSimpleName() + ". Unit needs deployment.");
        // The UI should be updated after this call.
    }

    public void deployUnit(Player player, Unit unit, int row, int col) throws CustomExceptions.InvalidPlacementException, CustomExceptions.InvalidActionException {
        if (player != getCurrentPlayer()) {
            throw new CustomExceptions.InvalidActionException("It's not your turn to deploy units!");
        }
        if (!player.getUnits().contains(unit)) {
            throw new CustomExceptions.InvalidActionException("This unit does not belong to you or has already been deployed.");
        }
        if (unit.getRow() != -1 || unit.getCol() != -1) {
            throw new CustomExceptions.InvalidActionException("This unit is already deployed.");
        }

        Block targetBlock = gameBoard[row][col];
        if (!(targetBlock instanceof EmptyBlock || targetBlock instanceof ForestBlock) || targetBlock.getUnit() != null || targetBlock.getStructure() != null) {
            throw new CustomExceptions.InvalidPlacementException("Cannot deploy unit here. Block is occupied or of an invalid type.");
        }

        unit.setPosition(row, col);
        targetBlock.setUnit(unit);
        GameLogger.log(player.getName() + " deployed a " + unit.getClass().getSimpleName() + " at (" + row + "," + col + ").");
        // The UI should be updated after this call.
    }


    public void handleUnitMovement(Player player, Unit unit, int targetRow, int targetCol) throws CustomExceptions.InvalidActionException, CustomExceptions.InvalidPlacementException {
        if (player != getCurrentPlayer() || !player.getUnits().contains(unit)) {
            throw new CustomExceptions.InvalidActionException("It's not your turn or you don't own this unit.");
        }
        if (unit.getRow() == -1 || unit.getCol() == -1) {
            throw new CustomExceptions.InvalidActionException("Unit is not deployed yet.");
        }


        unit.move(targetRow, targetCol);


        Block oldBlock = gameBoard[unit.getRow()][unit.getCol()];
        Block newBlock = gameBoard[targetRow][targetCol];

        if (oldBlock.getUnit() == unit) {
            oldBlock.setUnit(null);
        }
        unit.setPosition(targetRow, targetCol);
        newBlock.setUnit(unit);
        newBlock.onUnitEnter(unit);

        GameLogger.log(unit.getClass().getSimpleName() + " moved from (" + oldBlock.getRow() + "," + oldBlock.getCol() + ") to (" + targetRow + "," + targetCol + ").");
        // The UI should be updated after this call.
    }

    public void handleUnitAttack(Player player, Unit attacker, int targetRow, int targetCol) throws CustomExceptions.InvalidActionException {
        if (player != getCurrentPlayer() || !player.getUnits().contains(attacker)) {
            throw new CustomExceptions.InvalidActionException("It's not your turn or you don't own this unit.");
        }
        if (attacker.getRow() == -1 || attacker.getCol() == -1) {
            throw new CustomExceptions.InvalidActionException("Unit is not deployed yet.");
        }

        Block targetBlock = getBlockAt(targetRow, targetCol);
        if (targetBlock == null || (targetBlock.getUnit() == null && targetBlock.getStructure() == null)) {
            throw new CustomExceptions.InvalidActionException("No valid target at the specified location.");
        }

        if (targetBlock.getUnit() != null) {
            Unit defender = targetBlock.getUnit();
            if (defender.getOwner() == attacker.getOwner()) {
                throw new CustomExceptions.InvalidActionException("Cannot attack your own unit.");
            }
            attacker.attack(defender);
            if (defender.getHealth() <= 0) {
                GameLogger.log(defender.getClass().getSimpleName() + " at (" + defender.getRow() + "," + defender.getCol() + ") was destroyed.");
                defender.getOwner().removeUnit(defender);
                targetBlock.setUnit(null);
            }
        } else if (targetBlock.getStructure() != null) {
            Structure defender = targetBlock.getStructure();
            if (defender.getOwner() == attacker.getOwner()) {
                throw new CustomExceptions.InvalidActionException("Cannot attack your own structure.");
            }
            attacker.attack(defender);
            if (defender.getDurability() <= 0) {
                GameLogger.log(defender.getClass().getSimpleName() + " at (" + defender.getRow() + "," + defender.getCol() + ") was destroyed.");
                defender.getOwner().removeStructure(defender);
                if (defender instanceof TownHall) {
                    gameBoard[targetRow][targetCol] = new EmptyBlock(targetRow, targetCol);
                    checkWinCondition();
                } else {
                    targetBlock.setStructure(null);
                }
            }
        } else {
            throw new CustomExceptions.InvalidActionException("Invalid target for attack.");
        }
        // The UI should be updated after this call.
    }

    public void replaceBlock(int row, int col, Block newBlock) {
        if (row >= 0 && row < boardRows && col >= 0 && col < boardCols) {
            gameBoard[row][col] = newBlock;
            GameLogger.log("Block at (" + row + "," + col + ") replaced with " + newBlock.getClass().getSimpleName() + ".");
        } else {
            GameLogger.logError("Attempted to replace out-of-bounds block at (" + row + "," + col + ").");
        }
    }

    public List<Unit> getAllUnitsOnBoard() {
        List<Unit> allUnits = new ArrayList<>();
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                Block block = gameBoard[r][c];
                if (block != null && block.getUnit() != null) {
                    allUnits.add(block.getUnit());
                }
            }
        }
        return allUnits;
    }

    public List<Structure> getAllStructuresOnBoard() {
        List<Structure> allStructures = new ArrayList<>();
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                Block block = gameBoard[r][c];
                if (block != null && block.getStructure() != null) {
                    allStructures.add(block.getStructure());
                }
            }
        }
        return allStructures;
    }

    // Getters for UI access
    public Block[][] getGameBoard() {
        return gameBoard;
    }

    public int getBoardRows() {
        return boardRows;
    }

    public int getBoardCols() {
        return boardCols;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
