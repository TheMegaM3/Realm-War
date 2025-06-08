package com.realmwar.engine;

import com.realmwar.data.GameLogger;
import com.realmwar.engine.gamestate.GameOverState;
import com.realmwar.engine.gamestate.GameState;
import com.realmwar.engine.gamestate.RunningState;
import com.realmwar.model.*;
import com.realmwar.model.structures.Farm;
import com.realmwar.model.structures.Market;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.structures.TownHall;
import com.realmwar.model.units.Swordsman;
import com.realmwar.model.units.Unit;
import com.realmwar.util.Constants;
import com.realmwar.util.CustomExceptions.GameRuleException;

import java.util.List;
import java.util.stream.Collectors;

public class GameManager {

    private final GameBoard gameBoard;
    private final TurnManager turnManager;
    private final List<Player> players;
    private GameState currentState; // The polymorphic state object
    public Player winner;

    public GameManager(List<String> playerNames, int width, int height) {
        this.players = playerNames.stream()
                .map(name -> new Player(name, Constants.STARTING_GOLD, Constants.STARTING_FOOD))
                .collect(Collectors.toList());
        this.gameBoard = new GameBoard(width, height);
        this.turnManager = new TurnManager(this.players);
        this.currentState = new RunningState(this); // Set the initial state
        setupInitialState();
        GameLogger.log("GameManager created. " + getCurrentPlayer().getName() + "'s turn begins.");
    }

    private void setupInitialState() {
        placeEntity(new TownHall(players.get(0), 1, 1), 1, 1);
        if (players.size() > 1) {
            placeEntity(new TownHall(players.get(1), gameBoard.width - 2, gameBoard.height - 2), gameBoard.width - 2, gameBoard.height - 2);
        }
    }

    // --- PUBLIC API FOR THE CONTROLLER ---
    // These methods delegate behavior to the current state object.

    public void moveUnit(Unit unit, int toX, int toY) throws GameRuleException {
        currentState.moveUnit(unit, toX, toY);
    }

    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException {
        currentState.attackUnit(attacker, target);
    }

    public void nextTurn() {
        currentState.nextTurn();
    }

    // --- INTERNAL LOGIC (CALLED BY STATE OBJECTS) ---


    public void advanceTurn() {
        turnManager.nextTurn();
        Player currentPlayer = getCurrentPlayer();

        // Reset all of the new player's units so they can act.
        gameBoard.getUnitsForPlayer(currentPlayer).forEach(u -> u.setHasActedThisTurn(false));

        // Calculate resource generation and maintenance for the new player.
        int goldIncome = 0;
        int foodIncome = 0;
        int maintenanceCost = 0;
        for (Structure s : gameBoard.getStructuresForPlayer(currentPlayer)) {
            if (s instanceof Market) goldIncome += Constants.MARKET_GOLD_PRODUCTION;
            if (s instanceof Farm) foodIncome += Constants.FARM_FOOD_PRODUCTION;
            maintenanceCost += s.getMaintenanceCost();
        }

        currentPlayer.getResourceHandler().addResources(goldIncome, foodIncome);
        try {
            currentPlayer.getResourceHandler().spendResources(maintenanceCost, 0);
        } catch (GameRuleException e) {
            GameLogger.log(currentPlayer.getName() + " could not pay maintenance costs! (Deficit: " + (maintenanceCost - currentPlayer.getResourceHandler().getGold()) + " Gold)");
            // In a more complex game, you might add penalties here, like structures taking damage.
        }

        GameLogger.log("Turn ended. It is now " + currentPlayer.getName() + "'s turn.");
    }


    public void executeMove(Unit unit, int toX, int toY) throws GameRuleException {
        validateAction(unit);
        int distance = Math.abs(unit.getX() - toX) + Math.abs(unit.getY() - toY);
        if (distance > unit.getMovementRange()) throw new GameRuleException("Target is out of movement range.");

        GameTile targetTile = gameBoard.getTile(toX, toY);
        if (targetTile == null || targetTile.isOccupied()) throw new GameRuleException("Cannot move to an occupied or invalid tile.");

        placeEntity(null, unit.getX(), unit.getY()); // Remove from old tile
        placeEntity(unit, toX, toY);
        unit.setHasActedThisTurn(true);
        GameLogger.log(unit.getClass().getSimpleName() + " moved to (" + toX + "," + toY + ").");
    }


    public void executeAttack(Unit attacker, GameEntity target) throws GameRuleException {
        validateAction(attacker);
        int distance = Math.abs(attacker.getX() - target.getX()) + Math.abs(attacker.getY() - target.getY());
        if (distance > attacker.getAttackRange()) throw new GameRuleException("Target is out of attack range.");
        if (target.getOwner() == attacker.getOwner()) throw new GameRuleException("Cannot attack a friendly entity.");

        int baseDamage = attacker.getAttackPower();

        // --- Handle Special Abilities ---
        // Swordsman Cleave Attack
        if (attacker instanceof Swordsman) {
            List<Unit> adjacentTargets = gameBoard.getAdjacentUnits(target.getX(), target.getY());
            for (Unit secondaryTarget : adjacentTargets) {
                if (secondaryTarget != target && secondaryTarget.getOwner() != attacker.getOwner()) {
                    int cleaveDamage = baseDamage / Constants.SWORDSMAN_CLEAVE_DIVISOR;
                    secondaryTarget.takeDamage(cleaveDamage);
                    GameLogger.log("Cleave damage dealt to " + secondaryTarget.getClass().getSimpleName() + " for " + cleaveDamage + " damage!");
                    if (secondaryTarget.isDestroyed()) {
                        placeEntity(null, secondaryTarget.getX(), secondaryTarget.getY());
                        GameLogger.log(secondaryTarget.getClass().getSimpleName() + " was destroyed by cleave!");
                    }
                }
            }
        }

        // Apply primary damage
        target.takeDamage(baseDamage);
        GameLogger.log(attacker.getClass().getSimpleName() + " attacked " + target.getClass().getSimpleName() + " for " + baseDamage + " damage.");

        if (target.isDestroyed()) {
            placeEntity(null, target.getX(), target.getY());
            GameLogger.log(target.getClass().getSimpleName() + " was destroyed!");
            checkWinCondition();
        }
        attacker.setHasActedThisTurn(true);
    }


    private void validateAction(Unit unit) throws GameRuleException {
        if (unit.getOwner() != getCurrentPlayer()) throw new GameRuleException("It is not your turn.");
        if (unit.hasActedThisTurn()) throw new GameRuleException("This unit has already acted this turn.");
    }


    private void checkWinCondition() {
        for (Player p : players) {
            boolean hasTownHall = gameBoard.getStructuresForPlayer(p).stream().anyMatch(s -> s instanceof TownHall);
            if (!hasTownHall) {
                this.winner = players.stream().filter(player -> player != p).findFirst().orElse(null);
                this.currentState = new GameOverState(this, this.winner); // Transition to the Game Over state
                GameLogger.log("GAME OVER! Winner is " + winner.getName());
                return;
            }
        }
    }


    private void placeEntity(GameEntity entity, int x, int y) {
        gameBoard.placeEntity(entity, x, y);
    }

    // --- GETTERS (for Controller and View to read the model's state) ---
    public GameBoard getGameBoard() { return gameBoard; }
    public Player getCurrentPlayer() { return turnManager.getCurrentPlayer(); }
    public GameState getCurrentState() { return currentState; }
    public int getCurrentPlayerIndex() { return turnManager.getCurrentPlayerIndex(); }
    public void setCurrentPlayerIndex(int index) { turnManager.setCurrentPlayerIndex(index); }
    public List<Player> getPlayers() { return players; }
}
