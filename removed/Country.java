package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author zoli
 */
@Entity
public class Country implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    private String id;
    
    private String name;

    public Country() {
    }
    
    public Country(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + '#' + getId();
    }
    
}
