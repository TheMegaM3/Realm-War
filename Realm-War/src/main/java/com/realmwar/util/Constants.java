package com.realmwar.util;

/**
 * Central repository for all game balance constants and magic numbers.
 * Allows for easy tweaking of game rules and stats.
 */
public final class Constants {
    private Constants() {} // Private constructor to prevent instantiation

    // --- GAME ---
    public static final int STARTING_GOLD = 500;
    public static final int STARTING_FOOD = 200;

    // --- STRUCTURES ---
    public static final int TOWNHALL_DURABILITY = 1000;
    public static final int TOWNHALL_FOOD_PRODUCTION = 5;
    public static final int TOWNHALL_GOLD_PRODUCTION = 5;
    public static final int BARRACK_DURABILITY = 300;
    public static final int BARRACK_MAINTENANCE = 2;
    public static final int FARM_DURABILITY = 100;
    public static final int FARM_MAINTENANCE = 1;
    public static final int FARM_FOOD_PRODUCTION = 15;
    public static final int MARKET_DURABILITY = 120;
    public static final int MARKET_MAINTENANCE = 2;
    public static final int MARKET_GOLD_PRODUCTION = 10;
    public static final int TOWER_DURABILITY = 250;
    public static final int TOWER_MAINTENANCE = 3;
    public static final int TOWER_ATTACK_POWER = 15;
    public static final int TOWER_ATTACK_RANGE = 2;

    // --- UNITS ---
    // Costs
    public static final int PEASANT_GOLD_COST = 10;
    public static final int PEASANT_FOOD_COST = 5;
    public static final int SPEARMAN_GOLD_COST = 25;
    public static final int SPEARMAN_FOOD_COST = 10;
    public static final int SWORDSMAN_GOLD_COST = 50;
    public static final int SWORDSMAN_FOOD_COST = 25;
    public static final int KNIGHT_GOLD_COST = 80;
    public static final int KNIGHT_FOOD_COST = 40;

    // Stats
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
    public static final int SWORDSMAN_CLEAVE_DIVISOR = 2; // Damage dealt to secondary targets

    public static final int KNIGHT_HEALTH = 250;
    public static final int KNIGHT_ATTACK = 40;
    public static final int KNIGHT_RANGE = 1;
    public static final int KNIGHT_MOVE = 2; // Knights are faster
}
