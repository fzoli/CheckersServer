package org.dyndns.fzoli.mill.common.model.entity;

/**
 *
 * @author zoli
 */
public class Region {
    private final long ID;
    private final String COUNTRY, REGION_CODE, NAME;

    public Region(long id, String country, String regionCode, String name) {
        ID = id;
        COUNTRY = country;
        REGION_CODE = regionCode;
        NAME = name;
    }

    public long getId() {
        return ID;
    }

    public String getCountry() {
        return COUNTRY;
    }

    public String getRegionCode() {
        return REGION_CODE;
    }

    public String getName() {
        return NAME;
    }
    
}