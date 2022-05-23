package edu.uob;

import java.util.ArrayList;

public class Locations extends GameEntity{

    private ArrayList<Artefacts> artefacts = new ArrayList<>();
    private ArrayList<Furniture> furniture = new ArrayList<>();
    private ArrayList<Characters> characters = new ArrayList<>();
    private ArrayList<String> producedObject = new ArrayList<>();

    public Locations(String name, String description) {
        super(name, description);
    }

    public void addArtefacts(String name, String description){

        artefacts.add(new Artefacts(name, description));
    }
    public void addFurniture(String name, String description){

        furniture.add(new Furniture(name, description));
    }
    public void addCharacters(String name, String description){
        characters.add(new Characters(name,description));
    }

    public void addProducedObject(String object) {
        this.producedObject.add(object);
    }

    public ArrayList<Artefacts> getArtefacts() {
        return artefacts;
    }

    public ArrayList<Furniture> getFurniture() {
        return furniture;
    }

    public ArrayList<Characters> getCharacters() {
        return characters;
    }

    public ArrayList<String> getProducedObject() { return producedObject; }
}
