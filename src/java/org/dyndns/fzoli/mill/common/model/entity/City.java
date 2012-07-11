package org.dyndns.fzoli.mill.common.model.entity;

/**
 *
 * @author zoli
 */
public class City {
    
    private final long ID, REGION;
    private final double LATITUDE, LONGITUDE;
    private final int POPULATION;
    private final String NAME, ACCENT_NAME;

    public City(long id, long region, String name, String accentName, int population, double latitude, double longitude) {
        ID = id;
        REGION = region;
        NAME = name;
        ACCENT_NAME = accentName;
        POPULATION = population;
        LATITUDE = latitude;
        LONGITUDE = longitude;
    }

    public long getID() {
        return ID;
    }

    public long getRegion() {
        return REGION;
    }
    
    public String getName() {
        return NAME;
    }

    public String getAccentName() {
        return ACCENT_NAME;
    }
    
    public int getPopulation() {
        return POPULATION;
    }
    
    public double getLatitude() {
        return LATITUDE;
    }

    public double getLongitude() {
        return LONGITUDE;
    }

    @Override
    public String toString() {
        return getName() + '#' + getID();
    }
    
}