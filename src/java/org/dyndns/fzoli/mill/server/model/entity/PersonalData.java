package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.dyndns.fzoli.mill.common.model.entity.Sex;

/**
 *
 * @author zoli
 */
@Embeddable
public class PersonalData implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private boolean inverseName = false;
    
    private String firstName, lastName, country, region, city;

    @Temporal(TemporalType.DATE)
    private Date birthDate;
    
    @Enumerated(EnumType.STRING)
    private Sex sex;
    
    public PersonalData() {
    }

    public Sex getSex() {
        return sex;
    }
    
    public boolean isInverseName() {
        return inverseName;
    }
    
    public String getName() {
        return getFirstName() == null || getLastName() == null ? null :
               (!isInverseName() ? getFirstName() + " " + getLastName() : getLastName() + " " + getFirstName()).trim();
    }
    
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }
    
    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    public void setInverseName(boolean inverseName) {
        this.inverseName = inverseName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCity(String city) {
        this.city = city;
    }
    
}