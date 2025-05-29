package main.java.com.realmwar.game.util;

public class Constants {
    // Board dimensions
    public static final int BOARD_ROWS = 10;
    public static final int BOARD_COLS = 10;

    // Initial game values
    public static final int STARTING_GOLD = 500;
    public static final int STARTING_FOOD = 200;
    public static final int INITIAL_UNIT_SPACE = 5; // Initial unit space for each player

    // Block generation chance
    public static final double FOREST_BLOCK_CHANCE = 0.15; // 15% chance for Forest Block

    // --- TownHall ---
    public static final int TOWNHALL_DURABILITY = 500;
    public static final int TOWNHALL_GOLD_COST = 0;
    public static final int TOWNHALL_FOOD_COST = 0;
    // TownHall Max Level: 1 (cannot be upgraded)

    // --- Barrack ---
    public static final int BARRACK_DURABILITY = 100;
    public static final int BARRACK_MAINTENANCE = 10; // Maintenance cost per turn
    public static final int BARRACK_MAX_LEVEL = 3;
    public static final int BARRACK_GOLD_COST = 150;
    public static final int BARRACK_FOOD_COST = 50;
    public static final int BARRACK_INITIAL_UNIT_SPACE = 5; // Initial unit space provided
    public static final int BARRACK_UNIT_SPACE_PER_LEVEL = 3; // Increase in unit space per upgrade
    public static final int BARRACK_DURABILITY_PER_LEVEL = 50; // Increase in durability per upgrade

    // --- Farm ---
    public static final int FARM_DURABILITY = 80;
    public static final int FARM_MAINTENANCE = 5;
    public static final int FARM_MAX_LEVEL = 5;
    public static final int FARM_GOLD_COST = 100;
    public static final int FARM_FOOD_COST = 0;
    public static final int FARM_INITIAL_PRODUCTION = 20; // Initial food production
    public static final int FARM_PRODUCTION_PER_LEVEL = 10; // Increase in production per upgrade
    public static final int FARM_DURABILITY_PER_LEVEL = 20; // Increase in durability per upgrade

    // --- Market ---
    public static final int MARKET_DURABILITY = 80;
    public static final int MARKET_MAINTENANCE = 5;
    public static final int MARKET_MAX_LEVEL = 5;
    public static final int MARKET_GOLD_COST = 100;
    public static final int MARKET_FOOD_COST = 0;
    public static final int MARKET_INITIAL_PRODUCTION = 20; // Initial gold production
    public static final int MARKET_PRODUCTION_PER_LEVEL = 10; // Increase in production per upgrade
    public static final int MARKET_DURABILITY_PER_LEVEL = 20; // Increase in durability per upgrade

    // --- Tower ---
    public static final int TOWER_DURABILITY = 120;
    public static final int TOWER_MAINTENANCE = 15;
    public static final int TOWER_MAX_LEVEL = 4;
    public static final int TOWER_GOLD_COST = 200;
    public static final int TOWER_FOOD_COST = 100;
    public static final int TOWER_INITIAL_ATTACK_RANGE = 2;
    public static final int TOWER_ATTACK_RANGE_PER_LEVEL = 1;
    public static final int TOWER_INITIAL_ATTACK_POWER = 20;
    public static final int TOWER_ATTACK_POWER_PER_LEVEL = 10;
    public static final int TOWER_INITIAL_BLOCK_LEVEL = 2; // Blocks units below this level (e.g., Peasant = 1)
    public static final int TOWER_BLOCK_LEVEL_PER_LEVEL = 1; // Increase in blocking level per upgrade
    public static final int TOWER_DURABILITY_PER_LEVEL = 30; // Increase in durability per upgrade

    // --- Unit Costs & Stats ---
    // Peasant
    public static final int PEASANT_HEALTH = 20;
    public static final int PEASANT_MOVEMENT_RANGE = 2;
    public static final int PEASANT_ATTACK_POWER = 5;
    public static final int PEASANT_ATTACK_RANGE = 1;
    public static final int PEASANT_GOLD_COST = 30;
    public static final int PEASANT_FOOD_COST = 10;
    public static final int PEASANT_UNIT_SPACE_COST = 1;
    public static final int PEASANT_HIERARCHY_LEVEL = 1; // Unit hierarchy level

    // Spearman
    public static final int SPEARMAN_HEALTH = 40;
    public static final int SPEARMAN_MOVEMENT_RANGE = 2;
    public static final int SPEARMAN_ATTACK_POWER = 10;
    public static final int SPEARMAN_ATTACK_RANGE = 1;
    public static final int SPEARMAN_GOLD_COST = 60;
    public static final int SPEARMAN_FOOD_COST = 30;
    public static final int SPEARMAN_UNIT_SPACE_COST = 2;
    public static final int SPEARMAN_HIERARCHY_LEVEL = 2;

    // Swordsman
    public static final int SWORDSMAN_HEALTH = 60;
    public static final int SWORDSMAN_MOVEMENT_RANGE = 2;
    public static final int SWORDSMAN_ATTACK_POWER = 15;
    public static final int SWORDSMAN_ATTACK_RANGE = 1;
    public static final int SWORDSMAN_GOLD_COST = 100;
    public static final int SWORDSMAN_FOOD_COST = 50;
    public static final int SWORDSMAN_UNIT_SPACE_COST = 3;
    public static final int SWORDSMAN_HIERARCHY_LEVEL = 3;

    // Knight
    public static final int KNIGHT_HEALTH = 80;
    public static final int KNIGHT_MOVEMENT_RANGE = 3;
    public static final int KNIGHT_ATTACK_POWER = 25;
    public static final int KNIGHT_ATTACK_RANGE = 1;
    public static final int KNIGHT_GOLD_COST = 150;
    public static final int KNIGHT_FOOD_COST = 80;
    public static final int KNIGHT_UNIT_SPACE_COST = 4;
    public static final int KNIGHT_HIERARCHY_LEVEL = 4;

    // --- Combat Modifiers ---
    public static final int FOREST_ATTACK_ADVANTAGE = 5; // Extra attack power from Forest
}
