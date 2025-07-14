package com.realmwar.util;

/**
 * A utility class that holds all static final constants for the game.
 * This centralizes game balance numbers, making them easy to tweak.
 */
public final class Constants {
    // Private constructor to prevent instantiation of this utility class.
    private Constants() {}

    // --- Game Settings ---
    public static final int DEFAULT_BOARD_WIDTH = 10;
    public static final int DEFAULT_BOARD_HEIGHT = 10;
    public static final int STARTING_GOLD = 500;
    public static final int STARTING_FOOD = 200;

    // --- Timer Settings ---
    public static final int TURN_DURATION_SECONDS = 30;
    public static final int RESOURCE_TICK_MILLISECONDS = 3000; // Generate resources every 3 seconds.

    // --- Per-Tick Resource Generation ---
    public static final int EMPTY_BLOCK_GOLD_GENERATION = 1; // Gold per tick from an owned empty tile.
    public static final int FOREST_BLOCK_FOOD_GENERATION = 1; // Food per tick from an owned forest tile.
    public static final int FARM_FOOD_PER_TICK = 2;   // Base food per tick from a Farm structure (Level 1).
    public static final int MARKET_GOLD_PER_TICK = 1; // Base gold per tick from a Market structure (Level 1).
    public static final int FARM_FOOD_INCREMENT_PER_LEVEL = 2; // Additional food per tick per level for Farm.
    public static final int MARKET_GOLD_INCREMENT_PER_LEVEL = 1; // Additional gold per tick per level for Market.
    public static final int STRUCTURE_DURABILITY_INCREMENT_PER_LEVEL = 50; // Additional durability per level for structures.

    // --- Per-Turn Maintenance Costs ---
    // These costs are deducted once at the end of a player's turn.
    public static final int BARRACK_MAINTENANCE = 2;
    public static final int FARM_MAINTENANCE = 1;
    public static final int MARKET_MAINTENANCE = 1;
    public static final int TOWER_MAINTENANCE = 3;
    public static final int PEASANT_MAINTENANCE_COST = 1;
    public static final int SPEARMAN_MAINTENANCE_COST = 2;
    public static final int SWORDSMAN_MAINTENANCE_COST = 4;
    public static final int KNIGHT_MAINTENANCE_COST = 6;

    // --- Structure Stats ---
    public static final int TOWNHALL_DURABILITY = 1000;
    public static final int BARRACK_DURABILITY = 300;
    public static final int FARM_DURABILITY = 100;
    public static final int MARKET_DURABILITY = 120;
    public static final int TOWER_DURABILITY = 250;

    // Base build costs for structures.
    public static final int FARM_BUILD_COST = 100;
    public static final int BARRACK_BUILD_COST = 150;
    public static final int MARKET_BUILD_COST = 120;
    public static final int TOWER_BUILD_COST = 200;

    // Incremental cost added for each new structure of the same type.
    public static final int INCREMENTAL_BUILD_COST = 50;

    // Base cost for upgrading a structure (multiplied by current level).
    public static final int BASE_UPGRADE_COST = 75;

    // Structure build limits per player.
    public static final int MAX_FARMS_PER_PLAYER = 5;
    public static final int MAX_BARRACKS_PER_PLAYER = 3;
    public static final int MAX_MARKETS_PER_PLAYER = 3;
    public static final int MAX_TOWERS_PER_PLAYER = 4;

    // --- Unit Stats ---
    // Unit recruitment costs.
    public static final int PEASANT_GOLD_COST = 10;
    public static final int PEASANT_FOOD_COST = 5;
    public static final int SPEARMAN_GOLD_COST = 25;
    public static final int SPEARMAN_FOOD_COST = 10;
    public static final int SWORDSMAN_GOLD_COST = 50;
    public static final int SWORDSMAN_FOOD_COST = 25;
    public static final int KNIGHT_GOLD_COST = 80;
    public static final int KNIGHT_FOOD_COST = 40;

    // Combat and movement stats.
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
    public static final int TOWER_ATTACK_POWER = 15;
    public static final int TOWER_ATTACK_RANGE = 2;
}