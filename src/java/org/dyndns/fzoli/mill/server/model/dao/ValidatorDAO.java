package org.dyndns.fzoli.mill.server.model.dao;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.entity.Validator;

/**
 *
 * @author zoli
 */
public class ValidatorDAO extends AbstractDAO {
    
    private static Validator getValidator(Player player) {
        if (player == null) return null;
        try {
            TypedQuery<Validator> query = getEntityManager().createQuery("SELECT v FROM Validator v WHERE v.player = :player", Validator.class);
            query.setParameter("player", player);
            return query.getSingleResult();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public static Player getPlayer(String key) {
        if (key == null) return null;
        try {
            TypedQuery<Player> query = getEntityManager().createQuery("SELECT v.player FROM Validator v WHERE v.validatorKey = :key", Player.class);
            query.setParameter("key", key);
            return query.getSingleResult();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public static void setKey(Player player, String key) {
        if (player == null || key == null) return;
        Validator validator = getValidator(player);
        if (validator == null) validator = new Validator(player, key);
        else validator.setValidatorKey(key);
        EntityTransaction tr = getEntityManager().getTransaction();
        tr.begin();
        getEntityManager().persist(validator);
        tr.commit();
    }
    
    public static void removeKey(Player player) {
        if (player == null) return;
        try {
            EntityTransaction tr = getEntityManager().getTransaction();
            tr.begin();
            Query query = getEntityManager().createQuery("DELETE FROM Validator v WHERE v.player = :player");
            query.setParameter("player", player);
            query.executeUpdate();
            tr.commit();
        }
        catch (Exception ex) {
            ;
        }
    }
    
}