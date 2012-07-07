package org.dyndns.fzoli.mill.server.model;

import java.awt.image.RenderedImage;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.PlayerAvatarKeys;
import org.dyndns.fzoli.mill.common.key.PlayerAvatarReturn;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerAvatarData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerAvatarEvent;
import org.dyndns.fzoli.mill.server.model.dao.PlayerAvatarDAO;
import org.dyndns.fzoli.mill.server.model.entity.PlayerAvatar;
import org.dyndns.fzoli.mill.server.model.entity.Point;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerAvatarModel extends AbstractOnlineModel<PlayerAvatarEvent, PlayerAvatarData> implements PlayerAvatarKeys {
    
    private final static PlayerAvatarDAO DAO = new PlayerAvatarDAO();
    
    private Integer scale;
    private Point point;
    
    private PlayerAvatarReturn setAvatarAttrs(int x, int y, int scale) {
        if (x < 0 || y < 0 || scale < 0) return PlayerAvatarReturn.NOT_OK;
        this.scale = scale;
        this.point = new Point(x, y);
        return PlayerAvatarReturn.OK;
    }
    
    private PlayerAvatarReturn setAvatar(RenderedImage img) {
        String name = getPlayerName();
        if (name == null || scale == null || point == null) return PlayerAvatarReturn.NOT_OK;
        PlayerAvatar avatar = DAO.getPlayerAvatar(name);
        if (avatar == null) {
            avatar = new PlayerAvatar(name, img, point, scale);
        }
        else {
            avatar.setAvatar(img);
            avatar.setScale(scale);
            avatar.setTopLeftPoint(point);
        }
        DAO.save(avatar);
        scale = null;
        point = null;
        return PlayerAvatarReturn.OK;
    }

    private RenderedImage getAvatarImage() {
        PlayerAvatar a = getPlayerAvatar();
        if (a != null) return a.getAvatarImage();
        return null;
    }
    
    private PlayerAvatar getPlayerAvatar() {
        String name = getPlayerName();
        PlayerAvatar a = null;
        if (name != null) a = DAO.getPlayerAvatar(name);
        return a;
    }
    
    @Override
    protected RenderedImage getImage(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_GET_AVATAR)) return getAvatarImage();
        }
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
        return new PlayerAvatarData(getPlayerName(), point.getX(), point.getY(), scale);
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_SET_AVATAR_ATTRS)) {
                try {
                    int x = Integer.parseInt(rm.getFirst(KEY_X));
                    int y = Integer.parseInt(rm.getFirst(KEY_Y));
                    int scale = Integer.parseInt(rm.getFirst(KEY_SCALE));
                    return setAvatarAttrs(x, y, scale).ordinal();
                }
                catch (Exception ex) {
                    ;
                }
            }
        }
        return PlayerAvatarReturn.NOT_OK.ordinal();
    }
    
}