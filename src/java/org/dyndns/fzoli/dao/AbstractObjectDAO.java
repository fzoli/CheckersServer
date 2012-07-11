package org.dyndns.fzoli.dao;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 *
 * @author zoli
 */
public abstract class AbstractObjectDAO {
    
    private static final Map<String, EntityManager> MANAGERS = new HashMap<String, EntityManager>();
    
    protected abstract String getPath();
    
    protected <T> boolean save(T obj, Class<T> clazz) {
        EntityTransaction tr = getEntityManager().getTransaction();
        try {
            tr.begin();
            getEntityManager().persist(obj);
            tr.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    protected EntityManager getEntityManager() {
        EntityManager m = MANAGERS.get(getPath());
        if (m == null) {
            m = createEntityManager();
            MANAGERS.put(getPath(), m);
        }
        return m;
    }
    
    private EntityManager createEntityManager() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.user", "admin");
        properties.put("javax.persistence.jdbc.password", "admin");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb://localhost:6136/" + getPath(), properties);
        return emf.createEntityManager();
    }
    
}