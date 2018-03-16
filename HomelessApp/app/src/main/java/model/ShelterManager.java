package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michelleliu on 3/14/18.
 */

public class ShelterManager {
    private static final ShelterManager _instance = new ShelterManager();
    public static ShelterManager getInstance() { return _instance; }

    List<Shelter> shelterList;

    public List<Shelter> getShelterList() {
        return shelterList;
    }

    public void setShelterList(List<Shelter> shelterList) {
        this.shelterList = shelterList;
    }

    /**
     * Returns the Shelter whose name matches the input String exactly.
     * @param name of Shelter
     * @return Shelter object in shelterList
     */
    public Shelter findShelterByName(String name) {
        //TODO
        Shelter foundShelter = null;
        for (Shelter s : shelterList) {
            if (s.getName().equals(name)) {
                foundShelter = s;
            }
        }
        return foundShelter;
    }

    /**
     * Returns the List of Shelters whose names include String s
     * @param s String that is searched for
     * @return list of Shelters whose names include s
     */
    public List<Shelter> findShelterByString(String s) {
        List<Shelter> matchingShelters = new ArrayList<>();
        matchingShelters.add(shelterList.get(0));
        return matchingShelters;
    }

    // families, newborns
    public List<Shelter> findShelterByType() {
        //TODO fix stub
        return null;
    }

    public Shelter findShelterByKey(int key) {
        //TODO fix stub
        Shelter foundShelter = null;
        if (key >= shelterList.size()) {
            throw new IllegalArgumentException("Key is not in shelter list.");
        }
        for (Shelter s : shelterList) {
            if (s.getKey() == key) {
                foundShelter = s;
            }
        }
        return foundShelter;
    }



    //todo: add findShelterByType
}
