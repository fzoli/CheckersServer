package org.dyndns.fzoli.mill.server.model.dao;

import org.dyndns.fzoli.mill.server.model.entity.PlayerAvatar;
import org.dyndns.fzoli.mill.server.model.entity.Point;

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
    
//    public static void main(String[] args) {
//        new PlayerAvatarDAO().save(new PlayerAvatar("test", new byte[] {7, 8}, new Point(5, 6), 2));
//    }
    
}