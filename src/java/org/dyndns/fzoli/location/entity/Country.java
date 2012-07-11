package org.dyndns.fzoli.location.entity;

/**
 *
 * @author zoli
 */
public class Country extends Location {
    
    private final String ID, NAME;

    public Country(String id, String name) {
        ID = id;
        NAME = name;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return getName() + '#' + getID();
    }
    
    @Override
    public String getDisplay() {
        return getName();
    }
    
}