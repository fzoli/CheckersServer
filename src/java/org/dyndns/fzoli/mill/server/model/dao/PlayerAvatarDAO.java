package org.dyndns.fzoli.mill.server.model.dao;

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
        return null; //TODO
    }
    
    public boolean save(PlayerAvatar avatar) {
        return save(avatar, PlayerAvatar.class);
    }
    
}