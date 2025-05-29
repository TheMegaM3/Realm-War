package main.java.com.realmwar.game.entities;

import main.java.com.realmwar.game.core.GameManager;
import main.java.com.realmwar.game.core.ResourceHandler;
import main.java.com.realmwar.game.entities.blocks.EmptyBlock;
import main.java.com.realmwar.game.entities.structures.Barrack;
import main.java.com.realmwar.game.entities.structures.Structure;
import main.java.com.realmwar.game.entities.structures.TownHall;
import main.java.com.realmwar.game.entities.units.Unit;
import main.java.com.realmwar.game.util.Constants;
import main.java.com.realmwar.game.util.GameLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Player {
    private String name; // Player's name
    private ResourceHandler resourceHandler;
    private List<Structure> structures;
    private List<Unit> units;
    private TownHall townHall;
    private int maxUnitSpace;


    public Player(String name) {
        this.name = name;
        this.resourceHandler = new ResourceHandler(Constants.STARTING_GOLD, Constants.STARTING_FOOD);
        this.resourceHandler.setOwner(this);
        this.structures = new ArrayList<>();
        this.units = new ArrayList<>();
        this.maxUnitSpace = Constants.INITIAL_UNIT_SPACE;
        GameLogger.log("Player " + name + " created.");
    }


    public String getName() {
        return name;
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public List<Structure> getStructures() {
        return structures;
    }

    public List<Unit> getUnits() {
        return units;
    }


    public void addStructure(Structure structure) {
        this.structures.add(structure);
        if (structure instanceof TownHall) {
            this.townHall = (TownHall) structure;
            GameLogger.log(name + " now has a Town Hall.");
        } else if (structure instanceof Barrack) {
            updateMaxUnitSpace(((Barrack) structure).getUnitSpaceProvided());
        }
    }


    public void removeStructure(Structure structure) {
        this.structures.remove(structure);
        if (structure instanceof TownHall) {
            this.townHall = null;
            GameLogger.log(name + "'s Town Hall was destroyed!");
        } else if (structure instanceof Barrack) {
            updateMaxUnitSpace(-((Barrack) structure).getUnitSpaceProvided());
        }
        GameManager.getInstance().replaceBlock(structure.getRow(), structure.getCol(), new EmptyBlock(structure.getRow(), structure.getCol()));
    }


    public void addUnit(Unit unit) {
        if (getCurrentUnitSpace() < maxUnitSpace) {
            this.units.add(unit);
            GameLogger.log(name + " added unit " + unit.getClass().getSimpleName() + ". Current unit space: " + getCurrentUnitSpace() + "/" + maxUnitSpace + ".");
        } else {
            GameLogger.logWarning(name + " cannot add unit: Not enough unit space. Current: " + getCurrentUnitSpace() + ", Max: " + maxUnitSpace + ".");
        }
    }


    public void removeUnit(Unit unit) {
        this.units.remove(unit);
        GameLogger.log(name + " lost unit " + unit.getClass().getSimpleName() + ". Current unit space: " + getCurrentUnitSpace() + "/" + maxUnitSpace + ".");
    }

    public boolean hasTownHall() {
        return townHall != null && townHall.getDurability() > 0;
    }

    public int getCurrentUnitSpace() {

        return units.stream().mapToInt(Unit::getUnitSpaceCost).sum();
    }

    public int getMaxUnitSpace() {
        return maxUnitSpace;
    }

    public boolean canAddUnit() {
        return getCurrentUnitSpace() < maxUnitSpace;
    }

    public void updateMaxUnitSpace(int spaceChange) {
        this.maxUnitSpace += spaceChange;
        GameLogger.log(name + "'s Max unit space updated by " + spaceChange + ". New total: " + maxUnitSpace + ".");
    }
}
