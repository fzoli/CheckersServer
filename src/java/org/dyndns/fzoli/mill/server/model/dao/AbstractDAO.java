package org.dyndns.fzoli.mill.server.model.dao;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author zoli
 */
public abstract class AbstractDAO {
    
    private static EntityManager ENTITY_MANAGER = init();

    protected static EntityManager getEntityManager() {
        if (ENTITY_MANAGER == null) ENTITY_MANAGER = init();
        return ENTITY_MANAGER;
    }
    
    private static EntityManager createEntityManager() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.user", "admin");
        properties.put("javax.persistence.jdbc.password", "admin");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb://localhost:6136/mill.odb", properties);
        return emf.createEntityManager();
    }
    
    private static EntityManager init() {
        try {
            return ENTITY_MANAGER = createEntityManager();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}