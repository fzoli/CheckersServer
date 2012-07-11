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
public class Region implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private long id;
    
    private Country country;
    
    private String regionCode, name;

    public Region() {
    }

    public Region(Country country, String regionCode, String name) {
        this.country = country;
        this.regionCode = regionCode;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public Country getCountry() {
        return country;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + '#' + getId();
    }
    
}
