package org.dyndns.fzoli.mill.common.model.entity;

/**
 *
 * @author zoli
 */
public class Country {
    
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
        return getName() + "#" + getID();
    }
    
}