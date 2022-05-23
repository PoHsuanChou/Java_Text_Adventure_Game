package edu.uob;

import java.util.ArrayList;
import java.util.HashSet;

public class GameAction {
    private String Trigger;
    private  ArrayList<String> subjectsArray = new ArrayList<>();
    private  ArrayList<String> consumedArray = new ArrayList<>();
    private  ArrayList<String> producedArray = new ArrayList<>();
    private  ArrayList<String> narrativeArray = new ArrayList<>();

    public void setTrigger(String trigger) {
        Trigger = trigger;
    }

    public void addSubjectsArray(String subjects) {
        subjectsArray.add(subjects);
    }

    public void addConsumedArray(String consumed) {
        consumedArray.add(consumed);
    }

    public void addProducedArray(String produced) {
        producedArray.add(produced);
    }

    public void addNarrativeArray(String narration) {
        narrativeArray.add(narration);
    }

    public String getTrigger() {
        return Trigger;
    }

    public ArrayList<String> getConsumedArray() {
        return consumedArray;
    }

    public ArrayList<String> getSubjectsArray() {
        return subjectsArray;
    }

    public ArrayList<String> getProducedArray() {
        return producedArray;
    }

    public ArrayList<String> getNarrativeArray() {
        return narrativeArray;
    }
//    @Override
//    public String toString() {
//        return getConsumedArray().get(0);
//    }
//
//    public GameAction() {
//        super();
//    }
}
