package org.dyndns.fzoli.mill.server.model;

import java.awt.image.RenderedImage;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.PlayerAvatarKeys;
import org.dyndns.fzoli.mill.common.key.PlayerAvatarReturn;
import org.dyndns.fzoli.mill.common.model.pojo.BaseOnlinePojo;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerAvatarData;
import org.dyndns.fzoli.mill.server.model.dao.PlayerAvatarDAO;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerAvatarModel extends AbstractOnlineModel<BaseOnlinePojo, PlayerAvatarData> implements PlayerAvatarKeys {
    
    private final static PlayerAvatarDAO DAO = new PlayerAvatarDAO();
    
    private PlayerAvatarReturn setAvatar(RenderedImage img) {
        if (getPlayerName() == null) return PlayerAvatarReturn.NOT_OK;
        //TODO
        return null;
    }
    
    @Override
    protected int setImage(RenderedImage img, HttpServletRequest servletRequest, RequestMap request) {
        String action = request.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_SET_AVATAR)) return setAvatar(img).ordinal();
        }
        return PlayerAvatarReturn.NOT_OK.ordinal();
    }
    
    @Override
    protected PlayerAvatarData getProperties(HttpServletRequest hsr, RequestMap rm) {
        return new PlayerAvatarData(getPlayerName());
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }
    
}