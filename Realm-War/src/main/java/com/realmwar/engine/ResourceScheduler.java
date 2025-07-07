package com.realmwar.engine;

import com.realmwar.data.GameLogger;
import com.realmwar.model.Player;
import com.realmwar.model.structures.Farm;
import com.realmwar.model.structures.Market;
import com.realmwar.model.structures.Structure;
import com.realmwar.model.structures.TownHall;
import com.realmwar.util.Constants;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ResourceScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start(List<Player>  players, GameBoard board) {
        scheduler.scheduleAtFixedRate(() -> {
            for (Player player : players) {
                int gold = 0;
                int food = 0;

                List<Structure> structures = board.getStructuresForPlayer(player);
                for (Structure structure : structures) {
                    if (structure instanceof Market) {
                        gold += Constants.MARKET_GOLD_PRODUCTION;
                    }
                    if (structure instanceof Farm) {
                        food += Constants.FARM_FOOD_PRODUCTION;
                    }
                    if (structure instanceof TownHall){
                        gold += Constants.TOWNHALL_GOLD_PRODUCTION;
                        food += Constants.TOWNHALL_FOOD_PRODUCTION;
                    }
                }
                player.getResourceHandler().addResources(gold, food);
                GameLogger.log("[ResourceScheduler] " + player.getName() + " gained" + gold + " gold + " + food + " food");
            }
        }, 3000, 3000, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
