package edu.uob;

import java.util.ArrayList;
import java.util.HashMap;

public class Players{
    private String name;
    private int healthLevel = 3;
    private String location;
//    private final ArrayList<String> inventory = new ArrayList<>();
    private final HashMap<String,String> inventory = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealthLevel() {
        return healthLevel;
    }

    public void setHealthLevel(int winOrLose) {
        this.healthLevel = this.healthLevel + winOrLose;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public HashMap<String,String> getInventory() {
        return inventory;
    }
    public void addInventory(String object,String desc){
        inventory.put(object,desc);
    }


}
