package com.realmwar.util;

public final class Constants {
    private Constants() {}

    // --- تنظیمات بازی ---
    public static final int DEFAULT_BOARD_WIDTH = 10;
    public static final int DEFAULT_BOARD_HEIGHT = 10;
    public static final int STARTING_GOLD = 500;
    public static final int STARTING_FOOD = 200;

    // --- تنظیمات تایمر ---
    public static final int TURN_DURATION_SECONDS = 30;
    public static final int RESOURCE_TICK_MILLISECONDS = 3000; // هر 3 ثانیه

    // --- درآمد از زمین‌ها (در هر تیک) ---
    public static final int EMPTY_BLOCK_GOLD_GENERATION = 1;
    public static final int FOREST_BLOCK_FOOD_GENERATION = 1;


    // --- مشخصات ساختمان‌ها ---
    public static final int TOWNHALL_DURABILITY = 1000;
    public static final int BARRACK_DURABILITY = 300;
    public static final int FARM_DURABILITY = 100;
    public static final int MARKET_DURABILITY = 120;
    public static final int TOWER_DURABILITY = 250;

    // هزینه ساخت پایه
    public static final int FARM_BUILD_COST = 100;
    public static final int BARRACK_BUILD_COST = 150;
    public static final int MARKET_BUILD_COST = 120;
    public static final int TOWER_BUILD_COST = 200;

    // هزینه ساخت پلکانی (با هر ساخت جدید اضافه می‌شود)
    public static final int INCREMENTAL_BUILD_COST = 50;

    // هزینه ارتقا
    public static final int BASE_UPGRADE_COST = 75; // هزینه ارتقا = این مقدار * سطح فعلی

    // درآمد و هزینه نگهداری ساختمان‌ها (در هر نوبت کامل ۳۰ ثانیه‌ای)
    public static final int FARM_FOOD_PRODUCTION = 20;
    public static final int MARKET_GOLD_PRODUCTION = 15;
    public static final int BARRACK_MAINTENANCE = 2;
    public static final int FARM_MAINTENANCE = 1;
    public static final int MARKET_MAINTENANCE = 1;
    public static final int TOWER_MAINTENANCE = 3;


    // --- مشخصات سربازها ---
    // هزینه ساخت
    public static final int PEASANT_GOLD_COST = 10;
    public static final int PEASANT_FOOD_COST = 5;
    public static final int SPEARMAN_GOLD_COST = 25;
    public static final int SPEARMAN_FOOD_COST = 10;
    public static final int SWORDSMAN_GOLD_COST = 50;
    public static final int SWORDSMAN_FOOD_COST = 25;
    public static final int KNIGHT_GOLD_COST = 80;
    public static final int KNIGHT_FOOD_COST = 40;

    // هزینه نگهداری (در هر نوبت کامل ۳۰ ثانیه‌ای)
    public static final int PEASANT_MAINTENANCE_COST = 1;
    public static final int SPEARMAN_MAINTENANCE_COST = 2;
    public static final int SWORDSMAN_MAINTENANCE_COST = 4;
    public static final int KNIGHT_MAINTENANCE_COST = 6;

    // آمار مبارزه
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

    // آمار برج
    public static final int TOWER_ATTACK_POWER = 15;
    public static final int TOWER_ATTACK_RANGE = 2;
}
