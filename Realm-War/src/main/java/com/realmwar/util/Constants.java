// Constants.java
// Defines all game constants used in the RealmWar game.
// Contains settings for board dimensions, resources, unit stats, structure attributes, and game mechanics.

package com.realmwar.util;

// Final class to hold game constants, preventing instantiation
public final class Constants {
    // Private constructor to prevent instantiation
    private Constants() {}

    // --- Game Settings ---
    // Default dimensions of the game board
    public static final int DEFAULT_BOARD_WIDTH = 10;
    public static final int DEFAULT_BOARD_HEIGHT = 10;
    // Starting resources for each player
    public static final int STARTING_GOLD = 500;
    public static final int STARTING_FOOD = 200;

    // --- Timer Settings ---
    // Duration of a player's turn in seconds
    public static final int TURN_DURATION_SECONDS = 30;
    // Interval for resource generation in milliseconds
    public static final int RESOURCE_TICK_MILLISECONDS = 3000;

    // --- Per-Tick Resource Generation ---
    // Resource generation for terrain types
    public static final int EMPTY_BLOCK_GOLD_GENERATION = 1;
    public static final int FOREST_BLOCK_FOOD_GENERATION = 1;
    // Resource generation for structures
    public static final int FARM_FOOD_PER_TICK = 2;
    public static final int MARKET_GOLD_PER_TICK = 1;
    // Resource increments per structure level
    public static final int FARM_FOOD_INCREMENT_PER_LEVEL = 2;
    public static final int MARKET_GOLD_INCREMENT_PER_LEVEL = 1;
    // Durability increase per structure level
    public static final int STRUCTURE_DURABILITY_INCREMENT_PER_LEVEL = 50;

    // --- Per-Turn Maintenance Costs ---
    // Maintenance costs for structures
    public static final int BARRACK_MAINTENANCE = 2;
    public static final int FARM_MAINTENANCE = 1;
    public static final int MARKET_MAINTENANCE = 1;
    public static final int TOWER_MAINTENANCE = 3;
    // Maintenance costs for units
    public static final int PEASANT_MAINTENANCE_COST = 1;
    public static final int SPEARMAN_MAINTENANCE_COST = 2;
    public static final int SWORDSMAN_MAINTENANCE_COST = 4;
    public static final int KNIGHT_MAINTENANCE_COST = 6;

    // --- Structure Stats ---
    // Durability values for structures
    public static final int TOWNHALL_DURABILITY = 1000;
    public static final int BARRACK_DURABILITY = 300;
    public static final int FARM_DURABILITY = 100;
    public static final int MARKET_DURABILITY = 120;
    public static final int TOWER_DURABILITY = 250;

    // --- Structure Build Costs ---
    // Base build costs for structures
    public static final int FARM_BUILD_COST = 100;
    public static final int BARRACK_BUILD_COST = 150;
    public static final int MARKET_BUILD_COST = 120;
    public static final int TOWER_BUILD_COST = 200;
    // Incremental cost added for each additional structure of the same type
    public static final int INCREMENTAL_BUILD_COST = 50;
    // Base cost for upgrading a structure (multiplied by current level)
    public static final int BASE_UPGRADE_COST = 75;

    // --- Structure Build Limits ---
    // Maximum number of structures per player
    public static final int MAX_FARMS_PER_PLAYER = 5;
    public static final int MAX_BARRACKS_PER_PLAYER = 3;
    public static final int MAX_MARKETS_PER_PLAYER = 3;
    public static final int MAX_TOWERS_PER_PLAYER = 4;

    // --- Unit Stats ---
    // Costs for training units
    public static final int PEASANT_GOLD_COST = 10;
    public static final int PEASANT_FOOD_COST = 5;
    public static final int SPEARMAN_GOLD_COST = 25;
    public static final int SPEARMAN_FOOD_COST = 10;
    public static final int SWORDSMAN_GOLD_COST = 50;
    public static final int SWORDSMAN_FOOD_COST = 25;
    public static final int KNIGHT_GOLD_COST = 80;
    public static final int KNIGHT_FOOD_COST = 40;

    // Unit combat and movement stats
    public static final int PEASANT_HEALTH = 50;
    public static final int PEASANT_ATTACK = 5;
    public static final int PEASANT_RANGE = 1;
    public static final int PEASANT_MOVE = 1;

    public static final int SPEARMAN_HEALTH = 100;
    public static final int SPEARMAN_ATTACK = 15;
    public static final int SPEARMAN_RANGE = 1;
    public static final int SPEARMAN_MOVE = 1;

    public static final int SWORDSMAN_HEALTH = 150;
    public static final int SWORDSMAN_ATTACK = 25;
    public static final int SWORDSMAN_RANGE = 1;
    public static final int SWORDSMAN_MOVE = 1;

    public static final int KNIGHT_HEALTH = 250;
    public static final int KNIGHT_ATTACK = 40;
    public static final int KNIGHT_RANGE = 1;
    public static final int KNIGHT_MOVE = 2;

    // --- Tower Stats ---
    // Tower combat stats
    public static final int TOWER_ATTACK_POWER = 15;
    public static final int TOWER_ATTACK_RANGE = 2;

    // --- Unit Limits ---
    // Maximum number of units per player
    public static final int MAX_PEASANTS_PER_PLAYER = 10;
    public static final int MAX_SPEARMEN_PER_PLAYER = 8;
    public static final int MAX_SWORDSMEN_PER_PLAYER = 6;
    public static final int MAX_KNIGHTS_PER_PLAYER = 4;

    // --- Unit Space Settings ---
    // Unit space provided by structures
    public static final int BARRACK_BASE_UNIT_SPACE = 4; // Initial unit space for Barrack
    public static final int BARRACK_UNIT_SPACE_INCREMENT = 2; // Unit space increase per level
    public static final int TOWNHALL_UNIT_SPACE = 5; // Fixed unit space for TownHall
}