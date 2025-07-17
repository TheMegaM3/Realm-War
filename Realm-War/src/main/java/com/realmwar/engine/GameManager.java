package com.realmwar.engine;

import com.realmwar.data.GameLogger;
import com.realmwar.engine.blocks.ForestBlock;
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
    private int selectedX = -1; // Default to no selection
    private int selectedY = -1;
    private Unit selectedUnit;

    public GameManager(List<String> playerNames, int width, int height) {
        this.players = playerNames.stream()
                .map(name -> new Player(name, Constants.STARTING_GOLD, Constants.STARTING_FOOD))
                .collect(Collectors.toList());
        this.gameBoard = new GameBoard(width, height, players);
        this.turnManager = new TurnManager(this.players);
        this.currentState = new RunningState(this);
        this.selectedUnit = null;
        setupInitialState();
        GameLogger.log("GameManager created. " + getCurrentPlayer().getName() + "'s turn begins.");
    }

    private void setupInitialState() {
        if (players.isEmpty()) return;
        if (players.size() > 0) setupPlayerCorner(players.get(0), 1, 1);
        if (players.size() > 1) setupPlayerCorner(players.get(1), gameBoard.width - 2, gameBoard.height - 2);
        if (players.size() > 2) setupPlayerCorner(players.get(2), gameBoard.width - 2, 1);
        if (players.size() > 3) setupPlayerCorner(players.get(3), 1, gameBoard.height - 2);
    }

    private void setupPlayerCorner(Player player, int centerX, int centerY) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                GameTile tile = gameBoard.getTile(centerX + dx, centerY + dy);
                if (tile != null) {
                    if (dx == 0 && dy == 0) {
                        placeEntity(new TownHall(player, centerX, centerY), centerX, centerY);
                    } else {
                        tile.setOwner(player);
                    }
                }
            }
        }
    }

    private void applyTurnlyResourceChanges(Player player) {
        int goldFromTerritory = (int) gameBoard.getTerritorySize(player) * Constants.EMPTY_BLOCK_GOLD_GENERATION;
        if (goldFromTerritory > 0) {
            player.getResourceHandler().addResources(goldFromTerritory, 0);
            GameLogger.log(player.getName() + " gained " + goldFromTerritory + " gold from territory bonus.");
        }
    }

    public void advanceTurn() {
        Player endingPlayer = getCurrentPlayer();
        deductTurnlyMaintenance(endingPlayer);
        // MODIFIED: executeTowerAttacks call removed

        turnManager.nextTurn();
        Player currentPlayer = getCurrentPlayer();
        applyTurnlyResourceChanges(currentPlayer);

        gameBoard.getUnitsForPlayer(currentPlayer).forEach(u -> u.setHasActedThisTurn(false));
        setSelectedUnit(null);
        GameLogger.log("Turn ended for " + endingPlayer.getName() + ". It is now " + currentPlayer.getName() + "'s turn.");
    }

    public void applyPeriodicResourceChanges() {
        for (Player player : players) {
            int goldIncome = 0;
            int foodIncome = 0;

            for (Structure s : gameBoard.getStructuresForPlayer(player)) {
                if (s instanceof Market market) {
                    // MODIFIED: Correctly calls the new method
                    goldIncome += market.getGoldProduction();
                } else if (s instanceof Farm farm) {
                    // MODIFIED: Correctly calls the new method
                    foodIncome += farm.getFoodProduction();
                } else if (s instanceof TownHall) {
                    goldIncome += Constants.TOWNHALL_GOLD_PER_TICK;
                    foodIncome += Constants.TOWNHALL_FOOD_PER_TICK;
                }
            }
            if (goldIncome > 0 || foodIncome > 0) {
                player.getResourceHandler().addResources(goldIncome, foodIncome);
            }
        }
    }

    public void executeAttack(Unit attacker, GameEntity target) throws GameRuleException {
        validateAction(attacker);
        if (target == null) throw new GameRuleException("You must select a valid target.");

        int distance = Math.abs(attacker.getX() - target.getX()) + Math.abs(attacker.getY() - target.getY());
        if (distance > attacker.getAttackRange()) {
            throw new GameRuleException("Target is out of attack range.");
        }

        if (target.getOwner() == attacker.getOwner()) {
            throw new GameRuleException("Cannot attack a friendly entity.");
        }

        float attackMultiplier = 1.0f;
        if (gameBoard.getTile(attacker.getX(), attacker.getY()).getBlock() instanceof ForestBlock) {
            attackMultiplier += 0.25f;
        }
        int finalDamage = (int) (attacker.getAttackPower() * attackMultiplier);

        // MODIFIED: Unified call, passes GameBoard for protection checks.
        target.takeDamage(finalDamage, gameBoard);

        GameLogger.log(attacker.getClass().getSimpleName() + " attacked " + target.getClass().getSimpleName() + " for " + finalDamage + " damage.");

        if (target.isDestroyed()) {
            GameLogger.log(target.getClass().getSimpleName() + " at (" + target.getX() + "," + target.getY() + ") was destroyed!");
            if (target instanceof TownHall) {
                turnManager.removePlayer(target.getOwner());
            }
            if (target instanceof Unit) {
                target.getOwner().decrementUnitCount(target.getClass().getSimpleName());
            }
            placeEntity(null, target.getX(), target.getY());
            checkWinCondition();
        }

        attacker.setHasActedThisTurn(true);
    }

    // ... (rest of GameManager is the same as the last version I sent) ...
    public void moveUnit(Unit unit, int toX, int toY) throws GameRuleException { currentState.moveUnit(unit, toX, toY); }
    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException { currentState.attackUnit(attacker, target); }
    public void nextTurn() { currentState.nextTurn(); }
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
            }
        }
    }
    public void executeMove(Unit unit, int toX, int toY) throws GameRuleException {
        validateAction(unit);
        GameTile targetTile = gameBoard.getTile(toX, toY);
        if (!unit.canMoveTo(targetTile, gameBoard)) {
            throw new GameRuleException("Cannot move to the target tile. It might be occupied, out of range, or invalid territory.");
        }
        if (targetTile.getOwner() == null) {
            targetTile.setOwner(unit.getOwner());
        }
        gameBoard.placeEntity(null, unit.getX(), unit.getY());
        placeEntity(unit, toX, toY);
        unit.setHasActedThisTurn(true);
        GameLogger.log(unit.getClass().getSimpleName() + " moved to (" + toX + "," + toY + ").");
    }
    private void validateAction(Unit unit) throws GameRuleException {
        if (unit.getOwner() != getCurrentPlayer()) {
            throw new GameRuleException("It is not your turn.");
        }
        if (unit.hasActedThisTurn()) {
            throw new GameRuleException("This unit has already acted this turn.");
        }
    }
    private void checkWinCondition() {
        List<Player> playersWithTownHalls = players.stream()
                .filter(p -> gameBoard.getStructuresForPlayer(p).stream().anyMatch(s -> s instanceof TownHall))
                .toList();
        if (playersWithTownHalls.size() <= 1) {
            this.winner = playersWithTownHalls.isEmpty() ? null : playersWithTownHalls.get(0);
            this.currentState = new GameOverState(this, this.winner);
            String winnerName = this.winner != null ? this.winner.getName() : "No one";
            GameLogger.log("GAME OVER! Winner is " + winnerName);
        }
    }
    public void buildStructure(String structureType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);
        if (tile == null || tile.isOccupied() || !tile.getBlock().isBuildable()) {
            throw new GameRuleException("Cannot build on this tile!");
        }
        if (tile.getOwner() != currentPlayer && !gameBoard.isAdjacentToTerritory(x, y, currentPlayer)) {
            throw new GameRuleException("You must build inside or next to your territory.");
        }
        if (structureType.equals("Farm")) {
            if (!gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, TownHall.class) &&
                    !gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, Farm.class)) {
                throw new GameRuleException("Farms must be built next to a TownHall or another Farm.");
            }
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
            case "Farm" -> Constants.FARM_BUILD_COST;
            case "Barrack" -> Constants.BARRACK_BUILD_COST;
            case "Market" -> Constants.MARKET_BUILD_COST;
            case "Tower" -> Constants.TOWER_BUILD_COST;
            default -> throw new IllegalStateException("Unexpected value: " + structureType);
        };
        currentPlayer.getResourceHandler().spendResources(buildCost, 0);
        Structure structure = switch (structureType) {
            case "Farm" -> new Farm(currentPlayer, x, y);
            case "Barrack" -> new Barrack(currentPlayer, x, y);
            case "Market" -> new Market(currentPlayer, x, y);
            case "Tower" -> new Tower(currentPlayer, x, y);
            default -> throw new GameRuleException("Invalid structure type!");
        };
        placeEntity(structure, x, y);
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
        GameLogger.log(structure.getClass().getSimpleName() + " at (" + x + "," + y + ") upgraded to level " + structure.getLevel() + " for " + upgradeCost + " gold.");
    }
    public void mergeUnits(Unit unit1, Unit unit2) throws GameRuleException {
        if (unit1.getOwner() != getCurrentPlayer() || unit2.getOwner() != getCurrentPlayer())
            throw new GameRuleException("You can only merge your own units.");
        if (!unit1.getClass().equals(unit2.getClass()))
            throw new GameRuleException("Units must be of the same type to merge.");
        if (unit1.getUnitLevel() >= 4)
            throw new GameRuleException("Knights cannot be merged further.");
        if (Math.max(Math.abs(unit1.getX() - unit2.getX()), Math.abs(unit1.getY() - unit2.getY())) > 1) {
            throw new GameRuleException("Units must be adjacent to merge.");
        }
        Player owner = unit1.getOwner();
        int newX = unit2.getX();
        int newY = unit2.getY();
        String newUnitType = switch (unit1.getClass().getSimpleName()) {
            case "Peasant" -> "Spearman";
            case "Spearman" -> "Swordsman";
            case "Swordsman" -> "Knight";
            default -> throw new GameRuleException("This unit cannot be merged further.");
        };
        if (!owner.canTrainUnit(newUnitType)) {
            throw new GameRuleException("Cannot merge into " + newUnitType + ". Maximum limit reached (" + getMaxUnitLimit(newUnitType) + ").");
        }
        Unit newUnit = switch (newUnitType) {
            case "Spearman" -> new Spearman(owner, newX, newY);
            case "Swordsman" -> new Swordsman(owner, newX, newY);
            case "Knight" -> new Knight(owner, newX, newY);
            default -> throw new GameRuleException("Invalid unit type!");
        };
        owner.decrementUnitCount(unit1.getClass().getSimpleName());
        owner.decrementUnitCount(unit2.getClass().getSimpleName());
        owner.incrementUnitCount(newUnitType);
        placeEntity(null, unit1.getX(), unit1.getY());
        placeEntity(newUnit, newX, newY);
        GameLogger.log("Merged two " + unit1.getClass().getSimpleName() + "s into a " + newUnit.getClass().getSimpleName() + " at (" + newX + "," + newY + ").");
    }
    public void trainUnit(String unitType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);
        if (tile == null || tile.isOccupied()) {
            throw new GameRuleException("Target tile for training must be empty!");
        }
        if (tile.getOwner() != currentPlayer && !gameBoard.isAdjacentToTerritory(x, y, currentPlayer)) {
            throw new GameRuleException("You must train units inside or next to your territory.");
        }
        if (!currentPlayer.hasEnoughUnitSpace(gameBoard)) {
            throw new GameRuleException("Not enough unit space to train a new unit!");
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
            throw new GameRuleException("Units must be trained adjacent to the correct building (Barracks, or TownHall for Peasants).");
        }
        if (!currentPlayer.canTrainUnit(unitType)) {
            throw new GameRuleException("Cannot train " + unitType + ". Maximum limit reached (" + getMaxUnitLimit(unitType) + ").");
        }
        Unit newUnit = switch (unitType) {
            case "Peasant" -> new Peasant(currentPlayer, x, y);
            case "Spearman" -> new Spearman(currentPlayer, x, y);
            case "Swordsman" -> new Swordsman(currentPlayer, x, y);
            case "Knight" -> new Knight(currentPlayer, x, y);
            default -> throw new GameRuleException("Invalid unit type!");
        };
        currentPlayer.getResourceHandler().spendResources(newUnit.getGoldCost(), newUnit.getFoodCost());
        currentPlayer.incrementUnitCount(unitType);
        placeEntity(newUnit, x, y);
        GameLogger.log(currentPlayer.getName() + " trained a " + unitType + " at (" + x + "," + y + ").");
    }
    private int getMaxUnitLimit(String unitType) {
        return switch (unitType) {
            case "Peasant" -> Constants.MAX_PEASANTS_PER_PLAYER;
            case "Spearman" -> Constants.MAX_SPEARMAN_PER_PLAYER;
            case "Swordsman" -> Constants.MAX_SWORDSMEN_PER_PLAYER;
            case "Knight" -> Constants.MAX_KNIGHTS_PER_PLAYER;
            default -> 0;
        };
    }
    public GameBoard getGameBoard() { return gameBoard; }
    public Player getCurrentPlayer() { return turnManager.getCurrentPlayer(); }
    public GameState getCurrentState() { return currentState; }
    public int getCurrentPlayerIndex() { return turnManager.getCurrentPlayerIndex(); }
    public void setCurrentPlayerIndex(int index) { turnManager.setCurrentPlayerIndex(index); }
    public List<Player> getPlayers() { return players; }
    public void setSelectedTile(int x, int y) { this.selectedX = x; this.selectedY = y; }
    public int[] getSelectedTile() { return new int[]{selectedX, selectedY}; }
    void placeEntity(GameEntity entity, int x, int y) { gameBoard.placeEntity(entity, x, y); }
    public Unit getSelectedUnit() { return selectedUnit; }
    public void setSelectedUnit(Unit unit) { this.selectedUnit = unit; }
}
