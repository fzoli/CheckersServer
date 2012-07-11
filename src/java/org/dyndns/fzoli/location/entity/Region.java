package org.dyndns.fzoli.location.entity;

/**
 *
 * @author zoli
 */
public class Region implements Location {
    
    private final long ID;
    private final String COUNTRY, REGION_CODE, NAME;

    public Region(long id, String country, String regionCode, String name) {
        ID = id;
        COUNTRY = country;
        REGION_CODE = regionCode;
        NAME = name;
    }

    public long getID() {
        return ID;
    }

    public String getCountryID() {
        return COUNTRY;
    }

    public String getRegionCode() {
        return REGION_CODE;
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