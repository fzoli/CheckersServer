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

    protected boolean save(PlayerAvatar avatar) {
        return save(avatar, PlayerAvatar.class);
    }
    
}