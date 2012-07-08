package org.dyndns.fzoli.mill.server.model.dao;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.dyndns.fzoli.mill.server.model.entity.PlayerAvatar;

/**
 *
 * @author zoli
 */
public class PlayerAvatarDAO extends AbstractDAO {

    @Override
    protected String getPath() {
        return "mill_extend.odb";
    }

    public PlayerAvatar getPlayerAvatar(String playerName) {
        if (playerName == null) return null;
        try {
            TypedQuery<PlayerAvatar> query = getEntityManager().createQuery("SELECT a FROM PlayerAvatar a WHERE upper(a.playerName) = upper(:name)", PlayerAvatar.class);
            return query.setParameter("name", playerName).getSingleResult();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public boolean save(PlayerAvatar avatar) {
        return save(avatar, PlayerAvatar.class);
    }
    
}