package com.realmwar.engine;

import com.realmwar.data.GameLogger;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;
import com.realmwar.engine.gamestate.GameOverState;
import com.realmwar.engine.gamestate.GameState;
import com.realmwar.engine.gamestate.RunningState;
import com.realmwar.model.*;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;
import com.realmwar.util.Constants;
import com.realmwar.util.CustomExceptions.GameRuleException;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

// Class managing the overall game logic and state
public class GameManager {
    // The game board instance
    private final GameBoard gameBoard;
    // Manages turn progression
    private final TurnManager turnManager;
    // List of players in the game
    private final List<Player> players;
    // Current game state (e.g., Running, GameOver)
    private GameState currentState;
    // The winning player, if any
    public Player winner;
    // Coordinates of the currently selected tile
    private int selectedX, selectedY;
    // The currently selected unit
    private Unit selectedUnit;

    // Constructor to initialize the game with player names and board dimensions
    public GameManager(List<String> playerNames, int width, int height) {
        this.players = playerNames.stream()
                .map(name -> new Player(name, Constants.STARTING_GOLD, Constants.STARTING_FOOD))
                .collect(Collectors.toList());
        this.gameBoard = new GameBoard(width, height);
        this.turnManager = new TurnManager(this.players);
        this.currentState = new RunningState(this);
        this.selectedUnit = null;
        setupInitialState();
        GameLogger.log("GameManager created. " + getCurrentPlayer().getName() + "'s turn begins.");
    }

    // Sets up initial game state by placing TownHalls and initializing territories
    private void setupInitialState() {
        if (players.isEmpty()) return;

        int[][] positions = {
                {1, 1},
                {gameBoard.width - 2, gameBoard.height - 2},
                {gameBoard.width - 2, 1},
                {1, gameBoard.height - 2}
        };

        for (int i = 0; i < players.size(); i++) {
            int x = positions[i][0];
            int y = positions[i][1];
            placeEntity(new TownHall(players.get(i), x, y), x, y);
            gameBoard.initializePlayerTerritory(players.get(i), x, y);
        }
    }

    // Initiates a unit move through the current game state
    public void moveUnit(Unit unit, int toX, int toY) throws GameRuleException {
        currentState.moveUnit(unit, toX, toY);
    }

    // Initiates a unit attack through the current game state
    public void attackUnit(Unit attacker, GameEntity target) throws GameRuleException {
        currentState.attackUnit(attacker, target);
    }

    // Advances to the next turn through the current game state
    public void nextTurn() {
        currentState.nextTurn();
    }

    // Handles turn progression, including tower attacks and maintenance
    public void advanceTurn() {
        Player endingPlayer = getCurrentPlayer();
        executeTowerAttacks(endingPlayer);
        deductTurnlyMaintenance(endingPlayer);
        turnManager.nextTurn();
        Player currentPlayer = getCurrentPlayer();
        gameBoard.getUnitsForPlayer(currentPlayer).forEach(u -> u.setHasActedThisTurn(false));
        setSelectedUnit(null);
        GameLogger.log("Turn ended for " + endingPlayer.getName() + ". It is now " + currentPlayer.getName() + "'s turn.");
    }

    // Deducts maintenance costs for a player's units and structures
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

    // Applies periodic resource gains from structures
    public void applyPeriodicResourceChanges() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) return;

        int goldIncome = 0;
        int foodIncome = 0;

        for (Structure s : gameBoard.getStructuresForPlayer(currentPlayer)) {
            if (s instanceof Market market) {
                goldIncome += market.getGoldProduction();
            } else if (s instanceof Farm farm) {
                foodIncome += farm.getFoodProduction();
            }
        }

        if (goldIncome > 0 || foodIncome > 0) {
            currentPlayer.getResourceHandler().addResources(goldIncome, foodIncome);
            GameLogger.log(currentPlayer.getName() + " gained " + goldIncome + " gold and " + foodIncome + " food.");
        }
    }

    // Executes a unit move and updates territory
    public void executeMove(Unit unit, int toX, int toY) throws GameRuleException {
        validateAction(unit);
        int distance = Math.abs(unit.getX() - toX) + Math.abs(unit.getY() - toY);
        if (distance > unit.getMovementRange()) {
            throw new GameRuleException("Target is out of movement range.");
        }

        GameTile targetTile = gameBoard.getTile(toX, toY);
        if (targetTile == null) {
            throw new GameRuleException("Cannot move to an invalid tile.");
        }

        // Allow movement to any tile (occupied or not), and claim territory
        placeEntity(null, unit.getX(), unit.getY());// empty the previous tile
        placeEntity(unit, toX, toY); // move to new tile
        updateTerritory(unit.getOwner(), toX, toY, unit.getMovementRange()); //expands the territory around the new tile
        unit.setHasActedThisTurn(true);
        GameLogger.log(unit.getClass().getSimpleName() + " moved to (" + toX + "," + toY + ") and claimed territory.");
    }

    // Updates territory ownership around a point based on range
    private void updateTerritory(Player player, int x, int y, int range) {
        // Claim the target tile
        gameBoard.setTerritory(player, x, y);

        // Claim surrounding tiles based on the range
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                if (Math.abs(dx) + Math.abs(dy) <= range) { // Manhattan distance
                    int adjX = x + dx;
                    int adjY = y + dy;
                    GameTile adjTile = gameBoard.getTile(adjX, adjY);
                    if (adjTile != null) {
                        gameBoard.setTerritory(player, adjX, adjY);
                        GameLogger.log(player.getName() + " claimed territory at (" + adjX + "," + adjY + ").");
                    }
                }
            }
        }
    }

    // Updates territory for a Barrack's valid unit placement directions
    private void updateBarrackTerritory(Player player, Barrack barrack) {
        List<Point> validDirections = barrack.getValidUnitPlacementDirections();
        for (Point direction : validDirections) {
            int adjX = barrack.getX() + direction.x;
            int adjY = barrack.getY() + direction.y;
            GameTile adjTile = gameBoard.getTile(adjX, adjY);
            if (adjTile != null) {
                gameBoard.setTerritory(player, adjX, adjY);
                GameLogger.log(player.getName() + " claimed territory at (" + adjX + "," + adjY + ") due to Barrack upgrade.");
            }
        }
    }

    // Executes an attack by a unit on a target
    public void executeAttack(Unit attacker, GameEntity target) throws GameRuleException {
        validateAction(attacker);
        if (target == null) throw new GameRuleException("You must select a valid target.");
        int distance = Math.abs(attacker.getX() - target.getX()) + Math.abs(attacker.getY() - target.getY());
        if (distance > attacker.getAttackRange()) throw new GameRuleException("Target is out of attack range.");
        if (target.getOwner() == attacker.getOwner()) throw new GameRuleException("Cannot attack a friendly entity.");

        float attackMultiplier = 1.0f;
        if (gameBoard.getTile(attacker.getX(), attacker.getY()).block instanceof ForestBlock) {
            attackMultiplier += 0.25f;
        }
        int finalDamage = (int) (attacker.getAttackPower() * attackMultiplier);

        if (target instanceof Structure structure) {
            structure.takeDamage(finalDamage, gameBoard);
        } else {
            target.takeDamage(finalDamage);
        }
        GameLogger.log(attacker.getClass().getSimpleName() + " attacked " + target.getClass().getSimpleName() + " at (" + target.getX() + "," + target.getY() + ") for " + finalDamage + " damage.");

        if (target.isDestroyed()) {
            if (target instanceof TownHall) {
                turnManager.removePlayer(target.getOwner());
            }
            if (target instanceof Unit) {
                target.getOwner().decrementUnitCount(target.getClass().getSimpleName());
            }
            placeEntity(null, target.getX(), target.getY());
            GameLogger.log(target.getClass().getSimpleName() + " at (" + target.getX() + "," + target.getY() + ") was destroyed!");
            checkWinCondition();
        }
        attacker.setHasActedThisTurn(true);
    }

    // Executes automatic tower attacks on adjacent enemy units
    private void executeTowerAttacks(Player player) {
        for (Structure s : gameBoard.getStructuresForPlayer(player)) {
            if (s instanceof Tower tower) {
                gameBoard.getAdjacentUnits(tower.getX(), tower.getY()).stream()
                        .filter(unit -> unit.getOwner() != player)
                        .findFirst()
                        .ifPresent(enemyUnit -> {
                            enemyUnit.takeDamage(tower.getAttackPower());
                            GameLogger.log("Tower at (" + tower.getX() + "," + tower.getY() + ") attacked " +
                                    enemyUnit.getClass().getSimpleName() + " for " + tower.getAttackPower() + " damage.");
                            if (enemyUnit.isDestroyed()) {
                                enemyUnit.getOwner().decrementUnitCount(enemyUnit.getClass().getSimpleName());
                                placeEntity(null, enemyUnit.getX(), enemyUnit.getY());
                                GameLogger.log(enemyUnit.getClass().getSimpleName() + " was destroyed by a tower!");
                                checkWinCondition();
                            }
                        });
            }
        }
    }

    // Validates that a unit can perform an action
    private void validateAction(Unit unit) throws GameRuleException {
        if (unit.getOwner() != getCurrentPlayer()) throw new GameRuleException("It is not your turn.");
        if (unit.hasActedThisTurn()) throw new GameRuleException("This unit has already acted this turn.");
    }

    // Checks if the game has ended and sets the winner
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

    // Builds a new structure at the specified coordinates
    public void buildStructure(String structureType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        if (tile == null || tile.isOccupied() || !tile.block.isBuildable()) {
            throw new GameRuleException("Cannot build on this tile!");
        }

        // Check if the tile is in the current player's territory
        if (tile.getTerritoryOwner() != currentPlayer) {
            throw new GameRuleException("Can only build in your own territory!");
        }

        if (structureType.equals("Farm")) {
            if (!gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, TownHall.class) &&
                    !gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, Farm.class)) {
                throw new GameRuleException("Farms can only be built next to a TownHall or another Farm. Choose a valid tile!");
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
            case "Farm" -> Constants.FARM_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Barrack" -> Constants.BARRACK_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Market"  -> Constants.MARKET_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
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
        updateTerritory(currentPlayer, x, y, 1); // Structures claim adjacent tiles (range 1)
        if (structure instanceof Barrack barrack) {
            updateBarrackTerritory(currentPlayer, barrack);
        }
        GameLogger.log(currentPlayer.getName() + " built a " + structureType + " at (" + x + "," + y + ") for " + buildCost + " gold.");
    }

    // Upgrades a structure at the specified coordinates
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
        if (structure instanceof Barrack barrack) {
            updateBarrackTerritory(currentPlayer, barrack);
        }
        GameLogger.log(structure.getClass().getSimpleName() + " at ("+x+","+y+") upgraded to level " + structure.getLevel() + " for " + upgradeCost + " gold.");
    }

    // Merges two units into a stronger unit
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
        placeEntity(null, unit2.getX(), unit2.getY());
        placeEntity(newUnit, newX, newY);
        updateTerritory(owner, newX, newY, newUnit.getMovementRange());
        GameLogger.log("Merged two " + unit1.getClass().getSimpleName() + "s into a " + newUnit.getClass().getSimpleName() + " at ("+newX+","+newY+").");
    }

    // Trains a new unit at the specified coordinates
    public void trainUnit(String unitType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        if (tile == null || tile.isOccupied() || !tile.block.isBuildable()) {
            throw new GameRuleException("Target tile for training must be empty or buildable!");
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
            for (Structure structure : gameBoard.getStructuresForPlayer(currentPlayer)) {
                if (structure instanceof Barrack barrack) {
                    List<Point> validDirections = barrack.getValidUnitPlacementDirections();
                    for (Point direction : validDirections) {
                        int checkX = structure.getX() + direction.x;
                        int checkY = structure.getY() + direction.y;
                        if (checkX == x && checkY == y) {
                            canTrain = true;
                            break;
                        }
                    }
                    if (canTrain) break;
                }
            }
        }

        if (!canTrain) {
            throw new GameRuleException("Units must be trained on a tile adjacent to the correct building (Barrack for non-Peasants, or TownHall for Peasants) in a valid direction.");
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
        gameBoard.placeEntity(newUnit, x, y);
        updateTerritory(currentPlayer, x, y, newUnit.getMovementRange());
        GameLogger.log(currentPlayer.getName() + " trained a " + unitType + " at (" + x + "," + y + ") and claimed territory.");
    }

    // Returns the maximum limit for a specific unit type
    private int getMaxUnitLimit(String unitType) {
        return switch (unitType) {
            case "Peasant" -> Constants.MAX_PEASANTS_PER_PLAYER;
            case "Spearman" -> Constants.MAX_SPEARMEN_PER_PLAYER;
            case "Swordsman" -> Constants.MAX_SWORDSMEN_PER_PLAYER;
            case "Knight" -> Constants.MAX_KNIGHTS_PER_PLAYER;
            default -> 0;
        };
    }

    // Getter for the game board
    public GameBoard getGameBoard() { return gameBoard; }
    // Getter for the current player
    public Player getCurrentPlayer() { return turnManager.getCurrentPlayer(); }
    // Getter for the current game state
    public GameState getCurrentState() { return currentState; }
    // Getter for the current player index
    public int getCurrentPlayerIndex() { return turnManager.getCurrentPlayerIndex(); }
    // Setter for the current player index
    public void setCurrentPlayerIndex(int index) { turnManager.setCurrentPlayerIndex(index); }
    // Getter for the list of players
    public List<Player> getPlayers() { return players; }
    // Sets the selected tile coordinates
    public void setSelectedTile(int x, int y) { this.selectedX = x; this.selectedY = y; }
    // Getter for the selected tile coordinates
    public int[] getSelectedTile() { return new int[]{selectedX, selectedY}; }
    // Places an entity on the board
    void placeEntity(GameEntity entity, int x, int y) {
        gameBoard.placeEntity(entity, x, y);
    }
    // Getter for the selected unit
    public Unit getSelectedUnit() { return selectedUnit; }
    // Setter for the selected unit
    public void setSelectedUnit(Unit unit) { this.selectedUnit = unit; }
}