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

import static com.realmwar.util.Constants.RESOURCE_TICK_MILLISECONDS;
import static com.realmwar.util.Constants.TURN_DURATION_SECONDS;

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

        placeEntity(null, unit.getX(), unit.getY()); //doobdoob
        placeEntity(unit, toX, toY);
        if (targetTile.getOwner() == null) {
            targetTile.setOwner(unit.getOwner());
            GameLogger.log(unit.getOwner().getName() + " captured tile at (" + toX + "," + toY + ")");
        }
        unit.setHasActedThisTurn(true);
        GameLogger.log(unit.getClass().getSimpleName() + " moved to (" + toX + "," + toY + ").");

    }

    public void executeAttack(Unit attacker, GameEntity target) throws GameRuleException {
        validateAction(attacker);
        int distance = Math.abs(attacker.getX() - target.getX()) + Math.abs(attacker.getY() - target.getY());
        if (distance > attacker.getAttackRange()) throw new GameRuleException("Target is out of attack range.");
        if (target.getOwner() == attacker.getOwner()) throw new GameRuleException("Cannot attack a friendly entity.");

        float attackMultiplier = 1.0f;
        if (gameBoard.getTile(attacker.getX(), attacker.getY()).block instanceof com.realmwar.engine.blocks.ForestBlock) {
            attackMultiplier += 0.25f; // 25% قدرت بیشتر برای حمله از جنگل
        }
        int finalDamage = (int)(attacker.getAttackPower() * attackMultiplier);

        float defenseMultiplier = 1.0f;
        if (gameBoard.getTile(target.getX(), target.getY()).block instanceof com.realmwar.engine.blocks.ForestBlock) {
            defenseMultiplier -= 0.25f; // 25% آسیب کمتر هنگام دفاع در جنگل
        }
        int finalDamageToTake = Math.max(0, (int)(finalDamage * defenseMultiplier));

        // Apply damage
        target.takeDamage(finalDamageToTake);
        GameLogger.log(attacker.getClass().getSimpleName() + " attacked " + target.getClass().getSimpleName() + " for " + finalDamageToTake + " damage.");

        if (target.isDestroyed()) {
            placeEntity(null, target.getX(), target.getY());
            GameLogger.log(target.getClass().getSimpleName() + " was destroyed!");
            checkWinCondition();
        }
        attacker.setHasActedThisTurn(true);
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

        if (tile == null || tile.isOccupied() || !tile.block.isBuildable()) {
            throw new GameRuleException("Cannot build on this tile!");
        }

        // منطق هزینه ساخت پلکانی
        long existingCount = gameBoard.getStructuresForPlayer(currentPlayer).stream()
                .filter(s -> s.getClass().getSimpleName().equals(structureType))
                .count();

        int buildCost;
        switch (structureType) {
            case "Farm" -> buildCost = Constants.FARM_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Barrack" -> buildCost = Constants.BARRACK_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Market" -> buildCost = Constants.MARKET_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            case "Tower" -> buildCost = Constants.TOWER_BUILD_COST + (int) (existingCount * Constants.INCREMENTAL_BUILD_COST);
            default -> throw new GameRuleException("Invalid structure type!");
        }

        Structure structure = switch (structureType) {
            case "Farm" -> new Farm(currentPlayer, x, y);
            case "Barrack" -> new Barrack(currentPlayer, x, y);
            case "Market" -> new Market(currentPlayer, x, y);
            case "Tower" -> new Tower(currentPlayer, x, y);
            default -> throw new GameRuleException("Invalid structure type!");
        };

        currentPlayer.getResourceHandler().spendResources(buildCost, 0);
        gameBoard.placeEntity(structure, x, y);
    }

    public void upgradeStructure(int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

        GameEntity entity = tile.getEntity();
        if (!(entity instanceof Structure) || entity.getOwner() != currentPlayer) {
            throw new GameRuleException("You must select your own structure to upgrade.");
        }

        Structure structure = (Structure) entity;
        if (structure.getLevel() >= structure.getMaxLevel()) {
            throw new GameRuleException("This structure is already at max level.");
        }

        int upgradeCost = Constants.BASE_UPGRADE_COST * structure.getLevel();
        currentPlayer.getResourceHandler().spendResources(upgradeCost, 0);
        structure.levelUp();
        GameLogger.log(structure.getClass().getSimpleName() + " upgraded to level " + structure.getLevel());
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
        Unit newUnit;

        switch (unit1.getClass().getSimpleName()) {
            case "Peasant" -> newUnit = new Spearman(owner, newX, newY);
            case "Spearman" -> newUnit = new Swordsman(owner, newX, newY);
            case "Swordsman" -> newUnit = new Knight(owner, newX, newY);
            default -> throw new GameRuleException("This unit cannot be merged further.");
        }

        placeEntity(null, unit1.getX(), unit1.getY());
        placeEntity(null, unit2.getX(), unit2.getY());
        placeEntity(newUnit, newX, newY);
        GameLogger.log("Merged two units into a " + newUnit.getClass().getSimpleName());
    }


    public void trainUnit(String unitType, int x, int y) throws GameRuleException {
        Player currentPlayer = getCurrentPlayer();
        GameTile tile = gameBoard.getTile(x, y);

            if (tile == null || tile.isOccupied()) {
                throw new GameRuleException("Target tile for training must be empty!");
            }

            boolean canTrain = false;
            if (unitType.equals("Peasant")) { // Peasants can be trained next to TownHalls
                if (gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, TownHall.class)) {
                    canTrain = true;
                }
            }

            if (!unitType.equals("Peasant")) { // Other units are trained at Barracks
                if (gameBoard.isAdjacentToFriendlyStructure(x, y, currentPlayer, Barrack.class)) {
                    canTrain = true;
                }
            }

            if (!canTrain) {
                throw new GameRuleException("Units must be trained on a tile adjacent to the correct building (Barrack or TownHall for Peasants).");
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

    }

    /**
     * درآمدها و هزینه‌های نگهداری دوره‌ای را برای بازیکن فعلی محاسبه و اعمال می‌کند.
     * این متد به صورت بهینه تمام محاسبات را در یک حلقه انجام می‌دهد.
     */
    public void applyPeriodicResourceChanges() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) return;

        int goldIncome = 0;
        int foodIncome = 0;
        int maintenanceCost = 0; // هزینه نگهداری کل (از جنس طلا)

        // محاسبه درآمد و هزینه نگهداری ساختمان‌ها
        for (Structure s : gameBoard.getStructuresForPlayer(currentPlayer)) {
            if (s instanceof Market) {
                goldIncome += Constants.MARKET_GOLD_PRODUCTION;
            } else if (s instanceof Farm) {
                foodIncome += Constants.FARM_FOOD_PRODUCTION;
            }
            maintenanceCost += s.getMaintenanceCost();
        }

        for (int x = 0; x < gameBoard.width; x++) {
            for (int y = 0; y < gameBoard.height; y++) {
                GameTile tile = gameBoard.getTile(x, y);
                if (tile.getOwner() == currentPlayer) {
                    if (tile.block instanceof com.realmwar.engine.blocks.EmptyBlock) {
                        goldIncome += Constants.EMPTY_BLOCK_GOLD_GENERATION;
                    } else if (tile.block instanceof com.realmwar.engine.blocks.ForestBlock) {
                        foodIncome += Constants.FOREST_BLOCK_FOOD_GENERATION;
                    }
                }
            }
        }




        // محاسبه هزینه نگهداری سربازها
        for (Unit u : gameBoard.getUnitsForPlayer(currentPlayer)) {
            maintenanceCost += u.getMaintenanceCost();
        }

        // محاسبه درآمد و هزینه خالص برای اعمال در این تیک
        int ticksPerTurn = Constants.TURN_DURATION_SECONDS / (Constants.RESOURCE_TICK_MILLISECONDS / 1000);
        if (ticksPerTurn == 0) ticksPerTurn = 1; // جلوگیری از تقسیم بر صفر

        int netGold = (goldIncome - maintenanceCost) / ticksPerTurn;
        int netFood = foodIncome / ticksPerTurn;

        // اعمال منابع به بازیکن
        currentPlayer.getResourceHandler().addResources(netGold, netFood);
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