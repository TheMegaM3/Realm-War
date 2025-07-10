package com.realmwar.util;

import java.awt.*;

/**
 * Central repository for all game balance constants and magic numbers.
 * Allows for easy tweaking of game rules and stats.
 */
public final class Constants {
    private Constants() {} // Private constructor to prevent instantiation
    // تنظیمات بازی جدید
    public static final int DEFAULT_BOARD_WIDTH = 10;
    public static final int DEFAULT_BOARD_HEIGHT = 10;
    // --- GAME ---
    public static final int STARTING_GOLD = 500;
    public static final int STARTING_FOOD = 200;

    // --- STRUCTURES ---
    public static final int TOWNHALL_DURABILITY = 1000;
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

    // رنگ‌های اختصاصی Unitها
    //public static final Color PLAYER1_UNIT_COLOR = new Color(30, 80, 150);   // آبی تیره
//public static final Color PLAYER2_UNIT_COLOR = new Color(180, 40, 40);    // قرمز تیره

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

    public static final int FARM_BUILD_COST = 100;
    public static final int BARRACK_BUILD_COST = 150;
    public static final int MARKET_BUILD_COST = 120;
    public static final int TOWER_BUILD_COST = 200;

}
