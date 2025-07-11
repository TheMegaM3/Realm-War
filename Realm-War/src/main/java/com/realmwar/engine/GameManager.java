package com.realmwar.engine;

import com.realmwar.data.GameLogger;
import com.realmwar.engine.gamestate.GameOverState;
import com.realmwar.engine.gamestate.GameState;
import com.realmwar.engine.gamestate.RunningState;
import com.realmwar.model.*;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;
import com.realmwar.util.Constants;
import com.realmwar.util.CustomExceptions.GameRuleException;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The main controller of the game. It connects the game model (players, board)
 * with the game rules and actions triggered by the view.
 */
public class GameManager {
    private final GameBoard gameBoard;
    private final TurnManager turnManager;
    private final List<Player> players;
    private GameState currentState; // Manages the game's state (Running, GameOver, etc.)
    public Player winner;
    private int selectedX, selectedY; // Coordinates of the tile currently selected by the user.

    public GameManager(List<String> playerNames, int width, int height) {
        // Use Java Streams to cleanly map player names to Player objects.
        this.players = playerNames.stream()
                .map(name -> new Player(name, Constants.STARTING_GOLD, Constants.STARTING_FOOD))
                .collect(Collectors.toList());
        this.gameBoard = new GameBoard(width, height);
        this.turnManager = new TurnManager(this.players);
        this.currentState = new RunningState(this); // The game starts in the 'Running' state.
        setupInitialState();
        GameLogger.log("GameManager created. " + getCurrentPlayer().getName() + "'s turn begins.");
    }

    /**
     * Sets up the initial state of the board, placing starting Town Halls for each player.
     */
    private void setupInitialState() {
        if (players.isEmpty()) return;
        // Player 1 starts at top-left
        if (players.size() > 0) placeEntity(new TownHall(players.get(0), 1, 1), 1, 1);
        // Player 2 starts at bottom-right
        if (players.size() > 1) placeEntity(new TownHall(players.get(1), gameBoard.width - 2, gameBoard.height - 2), gameBoard.width - 2, gameBoard.height - 2);
        // Player 3 starts at top-right
        if (players.size() > 2) placeEntity(new TownHall(players.get(2), gameBoard.width - 2, 1), gameBoard.width - 2, 1);
        // Player 4 starts at bottom-left
        if (players.size() > 3) placeEntity(new TownHall(players.get(3), 1, gameBoard.height - 2), 1, gameBoard.height - 2);
    }

    // --- Public API for Game Actions ---
    // These methods delegate the action to the current state object (State Pattern).
    // This allows behavior to change (e.g., disable moves) when the game is over.

    public void moveUnit(Unit unit, int toX, int toY) throws GameRuleException {
        currentState.moveUnit(unit, toX, toY);
    }

    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException {
        currentState.attackUnit(attacker, target);
    }

    public void nextTurn() {
        currentState.nextTurn();
    }


    // --- Core Game Logic Methods ---

    /**
     * Advances the game to the next turn. This is the main heartbeat of the turn-based logic.
     */
    public void advanceTurn() {
        Player endingPlayer = getCurrentPlayer();

        // Let defensive structures attack automatically at the end of the turn.
        executeTowerAttacks(endingPlayer);
        // Deduct maintenance costs for the player whose turn is ending.
        deductTurnlyMaintenance(endingPlayer);

        // Switch to the next player.
        turnManager.nextTurn();
        Player currentPlayer = getCurrentPlayer();

        // Reset the "has acted" flag for all units belonging to the new player.
        gameBoard.getUnitsForPlayer(currentPlayer).forEach(u -> u.setHasActedThisTurn(false));
        GameLogger.log("Turn ended for " + endingPlayer.getName() + ". It is now " + currentPlayer.getName() + "'s turn.");
    }

    /**
     * Deducts all maintenance costs for a player's units and structures at once.
     * Called at the end of their turn.
     * @param player The player whose turn is ending.
     */
    private void deductTurnlyMaintenance(Player player) {
        int totalMaintenance = 0;
        totalMaintenance += gameBoard.getStructuresForPlayer(player).stream().mapToInt(Structure::getMaintenanceCost).sum();
        totalMaintenance += gameBoard.getUnitsForPlayer(player).stream().mapToInt(Unit::getMaintenanceCost).sum();

        if (totalMaintenance > 0) {
            GameLogger.log(player.getName() + "'s turn-end maintenance cost: " + totalMaintenance + " gold.");
            try {
                player.getResourceHandler().spendResources(totalMaintenance, 0);
            } catch (GameRuleException e) {
                GameLogger.log(player.getName() + " could not pay maintenance costs!");
                // Future enhancement: Add penalties for not paying upkeep.
            }
        }
    }

    /**
     * Adds periodic resource income to the current player.
     * This method is called by a timer in the GameFrame every few seconds.
     */
    public void applyPeriodicResourceChanges() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) return;

        int goldIncome = 0;
        int foodIncome = 0;

        // Calculate income from structures.
        for (Structure s : gameBoard.getStructuresForPlayer(currentPlayer)) {
            if (s instanceof Market) goldIncome += Constants.MARKET_GOLD_PER_TICK;
            else if (s instanceof Farm) foodIncome += Constants.FARM_FOOD_PER_TICK;
        }

        // Calculate income from controlled tiles.
        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                GameTile tile = gameBoard.getTile(x, y);
                if (tile.getOwner() == currentPlayer) {
                    if (tile.block instanceof EmptyBlock) goldIncome += Constants.EMPTY_BLOCK_GOLD_GENERATION;
                    else if (tile.block instanceof ForestBlock) foodIncome += Constants.FOREST_BLOCK_FOOD_GENERATION;
                }
            }
        }

        // Apply the calculated income.
        if (goldIncome > 0 || foodIncome > 0) {
            currentPlayer.getResourceHandler().addResources(goldIncome, foodIncome);
        }
    }

    /**
     * Contains the logic for executing a unit move action.
     */
    public void executeMove(Unit unit, int toX, int toY) throws GameRuleException {
        validateAction(unit); // Check if the action is legal.

        // Calculate Manhattan distance for movement validation.
        int distance = Math.abs(unit.getX() - toX) + Math.abs(unit.getY() - toY);
        if (distance > unit.getMovementRange()) {
            throw new GameRuleException("Target is out of movement range.");
        }

        GameTile targetTile = gameBoard.getTile(toX, toY);
        if (targetTile == null || targetTile.isOccupied()) {
            throw new GameRuleException("Cannot move to an occupied or invalid tile.");
        }

        placeEntity(null, unit.getX(), unit.getY()); // Clear the unit from its old tile.
        placeEntity(unit, toX, toY); // Place the unit on the new tile.

        // If the destination tile was neutral, capture it.
        if (targetTile.getOwner() == null) {
            targetTile.setOwner(unit.getOwner());
            GameLogger.log(unit.getOwner().getName() + " captured tile at (" + toX + "," + toY + ")");
        }

        unit.setHasActedThisTurn(true);
        GameLogger.log(unit.getClass().getSimpleName() + " moved to (" + toX + "," + toY + ").");
    }

    /**
     * Contains the logic for executing an attack action.
     */
    public void executeAttack(Unit attacker, GameEntity target) throws GameRuleException {
        validateAction(attacker); // Check if the action is legal.

        if (target == null) throw new GameRuleException("You must select a valid target.");

        int distance = Math.abs(attacker.getX() - target.getX()) + Math.abs(attacker.getY() - target.getY());
        if (distance > attacker.getAttackRange()) throw new GameRuleException("Target is out of attack range.");
        if (target.getOwner() == attacker.getOwner()) throw new GameRuleException("Cannot attack a friendly entity.");

        // Calculate damage with terrain modifiers.
        float attackMultiplier = 1.0f;
        if (gameBoard.getTile(attacker.getX(), attacker.getY()).block instanceof ForestBlock) {
            attackMultiplier += 0.25f; // 25% bonus for attacking from a forest.
        }
        int finalDamage = (int) (attacker.getAttackPower() * attackMultiplier);

        target.takeDamage(finalDamage);
        GameLogger.log(attacker.getClass().getSimpleName() + " attacked " + target.getClass().getSimpleName() + " at (" + target.getX() + "," + target.getY() + ") for " + finalDamage + " damage.");

        if (target.isDestroyed()) {
            placeEntity(null, target.getX(), target.getY());
            GameLogger.log(target.getClass().getSimpleName() + " at (" + target.getX() + "," + target.getY() + ") was destroyed!");
            checkWinCondition(); // Check for a winner whenever an entity is destroyed.
        }
        attacker.setHasActedThisTurn(true);
    }

    /**
     * Logic for defensive towers to attack automatically.
     */
    private void executeTowerAttacks(Player player) {
        for (Structure s : gameBoard.getStructuresForPlayer(player)) {
            if (s instanceof Tower tower) {
                // Find the first valid enemy target in range and attack it.
                gameBoard.getAdjacentUnits(tower.getX(), tower.getY()).stream()
                        .filter(unit -> unit.getOwner() != player)
                        .findFirst()
                        .ifPresent(enemyUnit -> {
                            enemyUnit.takeDamage(tower.getAttackPower());
                            GameLogger.log("Tower at (" + tower.getX() + "," + tower.getY() + ") attacked " +
                                    enemyUnit.getClass().getSimpleName() + " for " + tower.getAttackPower() + " damage.");
                            if (enemyUnit.isDestroyed()) {
                                placeEntity(null, enemyUnit.getX(), enemyUnit.getY());
                                GameLogger.log(enemyUnit.getClass().getSimpleName() + " was destroyed by a tower!");
                                checkWinCondition();
                            }
                        });
            }
        }
    }

    /**
     * A helper method to validate common action rules before execution.
     */
    private void validateAction(Unit unit) throws GameRuleException {
        if (unit.getOwner() != getCurrentPlayer()) throw new GameRuleException("It is not your turn.");
        if (unit.hasActedThisTurn()) throw new GameRuleException("This unit has already acted this turn.");
    }

    /**
     * Checks if a win condition has been met (i.e., only one player has a Town Hall left).
     */
    private void checkWinCondition() {
        List<Player> playersWithTownHalls = players.stream()
                .filter(p -> gameBoard.getStructuresForPlayer(p).stream().anyMatch(s -> s instanceof TownHall))
                .toList();

        if (playersWithTownHalls.size() <= 1 && players.size() > 1) {
            this.winner = playersWithTownHalls.isEmpty() ? null : playersWithTownHalls.get(0);
            this.currentState = new GameOverState(this, this.winner);
            String winnerName = this.winner != null ? this.winner.getName() : "No one";
            GameLogger.log("GAME OVER! Winner is " + winnerName);
        }
    }

    // ... (build, upgrade, merge, train methods are correct from previous fixes) ...
    public void buildStructure(String structureType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        if (tile == null || tile.isOccupied() || !tile.block.isBuildable()) {
            throw new GameRuleException("Cannot build on this tile!");
        }

        long existingCount = gameBoard.getStructuresForPlayer(currentPlayer).stream()
                .filter(s -> s.getClass().getSimpleName().equals(structureType))
                .count();

        int maxAllowed;
        switch (structureType) {
            case "Farm" -> maxAllowed = Constants.MAX_FARMS_PER_PLAYER;
            case "Barrack" -> maxAllowed = Constants.MAX_BARRACKS_PER_PLAYER;
            case "Market" -> maxAllowed = Constants.MAX_MARKETS_PER_PLAYER;
            case "Tower" -> maxAllowed = Constants.MAX_TOWERS_PER_PLAYER;
            default -> throw new GameRuleException("Invalid structure type!");
        }

        if (existingCount >= maxAllowed) {
            throw new GameRuleException("You have reached the maximum limit for " + structureType + "s (" + maxAllowed + ").");
        }

        int buildCost = switch (structureType) {
            case "Farm" -> Constants.FARM_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Barrack" -> Constants.BARRACK_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Market" -> Constants.MARKET_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Tower" -> Constants.TOWER_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            default -> throw new GameRuleException("Invalid structure type!");
        };

        Structure structure = switch (structureType) {
            case "Farm" -> new Farm(currentPlayer, x, y);
            case "Barrack" -> new Barrack(currentPlayer, x, y);
            case "Market" -> new Market(currentPlayer, x, y);
            case "Tower" -> new Tower(currentPlayer, x, y);
            default -> throw new GameRuleException("Invalid structure type!");
        };

        currentPlayer.getResourceHandler().spendResources(buildCost, 0);
        gameBoard.placeEntity(structure, x, y);
        GameLogger.log(currentPlayer.getName() + " built a " + structureType + " at (" + x + "," + y + ") for " + buildCost + " gold.");
    }

    public void upgradeStructure(int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameEntity entity = gameBoard.getTile(x, y).getEntity();
        if (!(entity instanceof Structure structure) || entity.getOwner() != currentPlayer) {
            throw new GameRuleException("You must select your own structure to upgrade.");
        }

        if (structure.getLevel() >= structure.getMaxLevel()) {
            throw new GameRuleException("This structure is already at max level.");
        }

        int upgradeCost = Constants.BASE_UPGRADE_COST * structure.getLevel();
        currentPlayer.getResourceHandler().spendResources(upgradeCost, 0);
        structure.levelUp();
        GameLogger.log(structure.getClass().getSimpleName() + " at ("+x+","+y+") upgraded to level " + structure.getLevel() + " for " + upgradeCost + " gold.");
    }

    public void mergeUnits(Unit unit1, Unit unit2) throws GameRuleException {
        if (unit1.getOwner() != getCurrentPlayer() || unit2.getOwner() != getCurrentPlayer())
            throw new GameRuleException("You can only merge your own units.");
        if (!unit1.getClass().equals(unit2.getClass()))
            throw new GameRuleException("Units must be of the same type to merge.");
        if (Math.abs(unit1.getX() - unit2.getX()) > 1 || Math.abs(unit1.getY() - unit2.getY()) > 1)
            throw new GameRuleException("Units must be adjacent to merge.");

        Player owner = unit1.getOwner();
        int newX = unit1.getX();
        int newY = unit1.getY();
        Unit newUnit = switch (unit1.getClass().getSimpleName()) {
            case "Peasant" -> new Spearman(owner, newX, newY);
            case "Spearman" -> new Swordsman(owner, newX, newY);
            case "Swordsman" -> new Knight(owner, newX, newY);
            default -> throw new GameRuleException("This unit cannot be merged further.");
        };

        placeEntity(null, unit1.getX(), unit1.getY());
        placeEntity(null, unit2.getX(), unit2.getY());
        placeEntity(newUnit, newX, newY);
        GameLogger.log("Merged two " + unit1.getClass().getSimpleName() + "s into a " + newUnit.getClass().getSimpleName() + " at ("+newX+","+newY+").");
    }

    public void trainUnit(String unitType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        if (tile == null || tile.isOccupied()) {
            throw new GameRuleException("Target tile for training must be empty!");
        }

        boolean canTrain = false;
        if (unitType.equals("Peasant")) {
            if (gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, TownHall.class)) {
                canTrain = true;
            }
        } else {
            if (gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, Barrack.class)) {
                canTrain = true;
            }
        }

        if (!canTrain) {
            throw new GameRuleException("Units must be trained on a tile adjacent to the correct building (Barrack, or TownHall for Peasants).");
        }

        Unit newUnit = switch (unitType) {
            case "Peasant" -> new Peasant(currentPlayer, x, y);
            case "Spearman" -> new Spearman(currentPlayer, x, y);
            case "Swordsman" -> new Swordsman(currentPlayer, x, y);
            case "Knight" -> new Knight(currentPlayer, x, y);
            default -> throw new GameRuleException("Invalid unit type!");
        };

        currentPlayer.getResourceHandler().spendResources(newUnit.getGoldCost(), newUnit.getFoodCost());
        gameBoard.placeEntity(newUnit, x, y);
        GameLogger.log(currentPlayer.getName() + " trained a " + unitType + " at (" + x + "," + y + ").");
    }


    // --- Getters and Setters ---
    public GameBoard getGameBoard() { return gameBoard; }
    public Player getCurrentPlayer() { return turnManager.getCurrentPlayer(); }
    public GameState getCurrentState() { return currentState; }
    public int getCurrentPlayerIndex() { return turnManager.getCurrentPlayerIndex(); }
    public void setCurrentPlayerIndex(int index) { turnManager.setCurrentPlayerIndex(index); }
    public List<Player> getPlayers() { return players; }
    public void setSelectedTile(int x, int y) { this.selectedX = x; this.selectedY = y; }
    public int[] getSelectedTile() { return new int[]{selectedX, selectedY}; }
    void placeEntity(GameEntity entity, int x, int y) {
        gameBoard.placeEntity(entity, x, y);
    }
}
