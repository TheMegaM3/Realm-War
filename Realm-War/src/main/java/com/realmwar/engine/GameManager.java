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

import java.util.List;
import java.util.stream.Collectors;

public class GameManager {
    private final GameBoard gameBoard;
    private final TurnManager turnManager;
    private final List<Player> players;
    private GameState currentState;
    public Player winner;
    private int selectedX, selectedY;

    public GameManager(List<String> playerNames, int width, int height) {
        this.players = playerNames.stream()
                .map(name -> new Player(name, Constants.STARTING_GOLD, Constants.STARTING_FOOD))
                .collect(Collectors.toList());
        this.gameBoard = new GameBoard(width, height);
        this.turnManager = new TurnManager(this.players);
        this.currentState = new RunningState(this);
        setupInitialState();
        GameLogger.log("GameManager created. " + getCurrentPlayer().getName() + "'s turn begins.");
    }

    private void setupInitialState() {
        placeEntity(new TownHall(players.get(0), 1, 1), 1, 1);
        if (players.size() > 1) {
            placeEntity(new TownHall(players.get(1), gameBoard.width - 2, gameBoard.height - 2),
                    gameBoard.width - 2, gameBoard.height - 2);
        }
    }

    // Public API methods
    public void moveUnit(Unit unit, int toX, int toY) throws GameRuleException {
        currentState.moveUnit(unit, toX, toY);
    }

    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException {
        currentState.attackUnit(attacker, target);
    }

    public void nextTurn() {
        currentState.nextTurn();
    }

    // Internal logic methods
    public void advanceTurn() {
        turnManager.nextTurn();
        Player currentPlayer = getCurrentPlayer();

        // بازنشانی وضعیت واحدها از طریق GameBoard
        gameBoard.getUnitsForPlayer(currentPlayer).forEach(u -> u.setHasActedThisTurn(false));

        // محاسبه منابع
        calculateResources(currentPlayer);

        GameLogger.log("Turn ended. It is now " + currentPlayer.getName() + "'s turn.");
    }

    private void calculateResources(Player player) {
        int goldIncome = 0;
        int foodIncome = 0;
        int maintenanceCost = 0;

        for (Structure s : gameBoard.getStructuresForPlayer(player)) {
            if (s instanceof Market) goldIncome += Constants.MARKET_GOLD_PRODUCTION;
            if (s instanceof Farm) foodIncome += Constants.FARM_FOOD_PRODUCTION;
            maintenanceCost += s.getMaintenanceCost();
        }

        player.getResourceHandler().addResources(goldIncome, foodIncome);
        try {
            player.getResourceHandler().spendResources(maintenanceCost, 0);
        } catch (GameRuleException e) {
            GameLogger.log(player.getName() + " could not pay maintenance costs!");
        }
    }

    public void executeMove(Unit unit, int toX, int toY) throws GameRuleException {
        validateAction(unit);
        int distance = Math.abs(unit.getX() - toX) + Math.abs(unit.getY() - toY);
        if (distance > unit.getMovementRange()) throw new GameRuleException("Target is out of movement range.");

        GameTile targetTile = gameBoard.getTile(toX, toY);
        if (targetTile == null || targetTile.isOccupied())
            throw new GameRuleException("Cannot move to an occupied or invalid tile.");

        placeEntity(null, unit.getX(), unit.getY());
        placeEntity(unit, toX, toY);
        unit.setHasActedThisTurn(true);
        GameLogger.log(unit.getClass().getSimpleName() + " moved to (" + toX + "," + toY + ").");

        checkAndEndTurn();
    }

    public void executeAttack(Unit attacker, GameEntity target) throws GameRuleException {
        validateAction(attacker);
        int distance = Math.abs(attacker.getX() - target.getX()) + Math.abs(attacker.getY() - target.getY());
        if (distance > attacker.getAttackRange()) throw new GameRuleException("Target is out of attack range.");
        if (target.getOwner() == attacker.getOwner()) throw new GameRuleException("Cannot attack a friendly entity.");

        // Apply damage
        target.takeDamage(attacker.getAttackPower());
        GameLogger.log(attacker.getClass().getSimpleName() + " attacked " +
                target.getClass().getSimpleName() + " for " +
                attacker.getAttackPower() + " damage.");

        if (target.isDestroyed()) {
            placeEntity(null, target.getX(), target.getY());
            GameLogger.log(target.getClass().getSimpleName() + " was destroyed!");
            checkWinCondition();
        }

        attacker.setHasActedThisTurn(true);
        checkAndEndTurn();
    }

    private void checkAndEndTurn() {
        boolean allUnitsActed = gameBoard.getUnitsForPlayer(getCurrentPlayer())
                .stream()
                .allMatch(Unit::hasActedThisTurn);

        if (allUnitsActed) {
            nextTurn();
        }
    }

    private void validateAction(Unit unit) throws GameRuleException {
        if (unit.getOwner() != getCurrentPlayer())
            throw new GameRuleException("It is not your turn.");
        if (unit.hasActedThisTurn())
            throw new GameRuleException("This unit has already acted this turn.");
    }

    private void checkWinCondition() {
        for (Player p : players) {
            boolean hasTownHall = gameBoard.getStructuresForPlayer(p).stream()
                    .anyMatch(s -> s instanceof TownHall);
            if (!hasTownHall) {
                this.winner = players.stream()
                        .filter(player -> player != p)
                        .findFirst()
                        .orElse(null);
                this.currentState = new GameOverState(this, this.winner);
                GameLogger.log("GAME OVER! Winner is " + winner.getName());
                return;
            }
        }
    }

    public void buildStructure(String structureType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        if (tile == null || tile.isOccupied() || tile.getEntity() != null) {
            throw new GameRuleException("This tile is not buildable!");
        }

        Structure structure = switch (structureType) {
            case "Farm" -> new Farm(currentPlayer, x, y);
            case "Barrack" -> new Barrack(currentPlayer, x, y);
            case "Market" -> new Market(currentPlayer, x, y);
            case "Tower" -> new Tower(currentPlayer, x, y);
            default -> throw new GameRuleException("Invalid structure type!");
        };

        currentPlayer.getResourceHandler().spendResources(structure.getBuildCost(), 0);
        gameBoard.placeEntity(structure, x, y);
        checkAndEndTurn();
    }

    public void trainUnit(String unitType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        if (tile == null || tile.isOccupied()) {
            throw new GameRuleException("Tile is invalid or already occupied!");
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
        checkAndEndTurn();
    }

    // Getters and Setters
    public GameBoard getGameBoard() { return gameBoard; }
    public Player getCurrentPlayer() { return turnManager.getCurrentPlayer(); }
    public GameState getCurrentState() { return currentState; }
    public int getCurrentPlayerIndex() { return turnManager.getCurrentPlayerIndex(); }
    public void setCurrentPlayerIndex(int index) { turnManager.setCurrentPlayerIndex(index); }
    public List<Player> getPlayers() { return players; }
    public void setSelectedTile(int x, int y) { this.selectedX = x; this.selectedY = y; }
    public int[] getSelectedTile() { return new int[]{selectedX, selectedY}; }
    private void placeEntity(GameEntity entity, int x, int y) {
        gameBoard.placeEntity(entity, x, y);
    }
}