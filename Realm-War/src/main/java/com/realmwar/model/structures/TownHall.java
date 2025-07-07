package com.realmwar.model.structures;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Structure;
import com.realmwar.util.Constants;
public class TownHall extends Structure {
    private final int goldProduction;
    private final int foodProduction;
    public TownHall(Player o, int x, int y) {
        super(o, x, y, Constants.TOWNHALL_DURABILITY, 0);
        this.foodProduction = Constants.TOWNHALL_FOOD_PRODUCTION;
        this.goldProduction = Constants.TOWNHALL_GOLD_PRODUCTION;
    }
    public int getGoldProduction() {
        return goldProduction;
    }

    public int getFoodProduction() {
        return foodProduction;
    }
}