package com.puradox.diorite.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class LoadoutConfiguration { //The heart.
    public List<String> nameFilters;
    public List<String> itemFilters;
    public List<String> nbtStringFilters;

    public int id;
    public String name;
    public int leaveStacksForBuilding;

    public boolean selected = false;

    public boolean modifiable;

    public LoadoutConfiguration() {
        this.nameFilters = new ArrayList<>();
        this.itemFilters = new ArrayList<>();
        this.nbtStringFilters = new ArrayList<>();
        
        this.modifiable=true;
    }

    public LoadoutConfiguration(LoadoutConfiguration loadout) {
        this.nameFilters = new ArrayList<>(loadout.nameFilters);
        this.itemFilters = new ArrayList<>(loadout.itemFilters);
        this.nbtStringFilters = new ArrayList<>(loadout.nbtStringFilters);


        this.name = loadout.name;
        this.modifiable = loadout.modifiable;
        this.leaveStacksForBuilding=loadout.leaveStacksForBuilding;
        this.id = loadout.id;

        this.selected=loadout.isSelected();
    }
    public LoadoutConfiguration(int id) {
        this.nameFilters = new ArrayList<>();
        this.itemFilters = new ArrayList<>();
        this.nbtStringFilters = new ArrayList<>();
        
        this.modifiable=true;
        this.id=id;
    }

    public LoadoutConfiguration(List<String> nameFilters, List<String> itemFilters, List<String> nbtStringFilters, int id) {
        this.nameFilters=nameFilters;
        this.itemFilters=itemFilters;
        this.nbtStringFilters = nbtStringFilters;
        
        this.id = id;
        this.leaveStacksForBuilding = 0;
        this.modifiable=true;
    }

    public LoadoutConfiguration(List<String> nameFilters, List<String> itemFilters, List<String> nbtStringFilters, int id, String name) {
        this.nameFilters=nameFilters;
        this.itemFilters=itemFilters;
        this.nbtStringFilters = nbtStringFilters;
        
        this.name = name;
        this.id = id;
        this.leaveStacksForBuilding = 0;
        this.modifiable=true;
    }

    public LoadoutConfiguration(List<String> nameFilters, List<String> itemFilters, List<String> nbtStringFilters, int id, String name, boolean modifiable) {
        this.nameFilters=nameFilters;
        this.itemFilters=itemFilters;
        this.nbtStringFilters = nbtStringFilters;
        
        this.name = name;
        this.id = id;
        this.leaveStacksForBuilding = 0;
        this.modifiable=modifiable;
    }


    public LoadoutConfiguration(List<String> nameFilters, List<String> itemFilters, List<String> nbtStringFilters, int id, String name, int leaveStacksForBuilding) {
        this.nameFilters=nameFilters;
        this.itemFilters=itemFilters;
        this.nbtStringFilters = nbtStringFilters;
        
        this.name = name;
        this.id = id;
        this.leaveStacksForBuilding = leaveStacksForBuilding;
        this.modifiable=true;
    }

    public LoadoutConfiguration(List<String> nameFilters, List<String> itemFilters, List<String> nbtStringFilters, int id, String name, boolean modifiable, int leaveStacksForBuilding) {
        this.nameFilters=nameFilters;
        this.itemFilters=itemFilters;
        this.nbtStringFilters = nbtStringFilters;
        
        this.name = name;
        this.id = id;
        this.leaveStacksForBuilding = leaveStacksForBuilding;
        this.modifiable=modifiable;
    }

    public boolean isSelected() {
        return selected;
    }

}
