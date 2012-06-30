package org.dyndns.fzoli.mill.server.model.dao;

import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.entity.Validator;

/**
 *
 * @author zoli
 */
public class ValidatorDAO extends AbstractDAO {

    public static Validator getValidator(String key) {
        if (key == null) return null;
        try {
            TypedQuery<Validator> query = getEntityManager().createQuery("SELECT v FROM Validator v WHERE v.validatorKey = :key", Validator.class);
            query.setParameter("key", key);
            return query.getSingleResult();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public static Player getPlayer(String key) {
        if (key == null) return null;
        Validator validator = getValidator(key);
        return validator == null ? null : validator.getPlayer();
    }
    
    public static void setKey(Player player, String key) {
        if (player == null || key == null) return;
        Validator validator = getValidator(key);
        if (validator == null) validator = new Validator(player, key);
        else validator.setValidatorKey(key);
        save(validator);
    }
    
    public static void save(Validator validator) {
        EntityTransaction tr = getEntityManager().getTransaction();
        tr.begin();
        getEntityManager().persist(validator);
        tr.commit();
    }
    
}