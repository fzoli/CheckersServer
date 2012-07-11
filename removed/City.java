package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 * @author zoli
 */
@Entity
public class City implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private long id;
    
    private Region region;
    private double latitude, longitude;
    private String name, accentName;

    public City() {
    }

    public City(Region region, String name, String accentName, double latitude, double longitude) {
        this.region = region;
        this.name = name;
        this.accentName = accentName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public Region getRegion() {
        return region;
    }
    
    public String getName() {
        return name;
    }

    public String getAccentName() {
        return accentName;
    }
    
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return getName() + '#' + getId();
    }
    
}
