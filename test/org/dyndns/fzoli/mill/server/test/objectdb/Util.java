package org.dyndns.fzoli.mill.server.test.objectdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.*;

/**
 * Player's online statuses.
 * @author zoli
 */
enum OnlineStatus {
    ONLINE,
    INVISIBLE
}

/**
 * Helper class.
 * @author zoli
 */
class Util {
    
    static final String PLAYER1 = "player1", PLAYER2 = "player2";
    
    static EntityManager createEntityManager() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.user", "admin");
        properties.put("javax.persistence.jdbc.password", "admin");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb://localhost:6136/fzoli_bugreport.odb", properties);
        return emf.createEntityManager();
    }
    
    static long getCount(EntityManager db, Class<?> entityClass) {
        try {
            return db.createQuery("SELECT count(e) FROM " + entityClass.getName() + " e", Long.class).getSingleResult();
        }
        catch (PersistenceException ex) {
            return 0;
        }
    }
    
    static void remove(EntityManager db, Class<?> entityClass) {
        EntityTransaction tr = db.getTransaction();
        tr.begin();
        if (getCount(db, entityClass) != 0) db.createQuery("DELETE FROM " + entityClass.getName() + " e").executeUpdate();
        tr.commit();
    }
    
    static void clearDatabase(EntityManager db) {
        remove(db, Player.class);
        remove(db, Message.class);
    }
    
    static Player getPlayer(EntityManager db, String name) {
        TypedQuery<Player> query = db.createQuery("SELECT p FROM Player p WHERE upper(p.playerName) = upper(:name)", Player.class);
        try {
            return query.setParameter("name", name).getSingleResult();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static List<Message> getMessages(EntityManager db, String senderPlayerName) {
        TypedQuery<Message> query = db.createQuery("SELECT m FROM Message m WHERE m.sender.playerName = :name", Message.class);
        try {
            return query.setParameter("name", senderPlayerName).getResultList();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static boolean save(EntityManager db, Object obj) {
        EntityTransaction tr = db.getTransaction();
        try {
            tr.begin();
            db.persist(obj);
            tr.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
}