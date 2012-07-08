package org.dyndns.fzoli.mill.server.model;

import com.thebuzzmedia.imgscalr.Scalr;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.PlayerAvatarKeys;
import org.dyndns.fzoli.mill.common.key.PlayerAvatarReturn;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerAvatarData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerAvatarEvent;
import org.dyndns.fzoli.mill.common.permission.Permission;
import org.dyndns.fzoli.mill.server.Resource;
import org.dyndns.fzoli.mill.server.model.dao.PlayerAvatarDAO;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.entity.PlayerAvatar;
import org.dyndns.fzoli.mill.server.model.entity.Point;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerAvatarModel extends AbstractOnlineModel<PlayerAvatarEvent, PlayerAvatarData> implements PlayerAvatarKeys {
    
    //TODO: avatar változás esemény csak akkor, ha (p.getOnlineStatus().equals(OnlineStatus.ONLINE) || me.canUsePermission(p, Permission.INVISIBLE_STATUS_DETECT))
    
    private final static PlayerDAO PDAO = new PlayerDAO();
    private final static PlayerAvatarDAO DAO = new PlayerAvatarDAO();
    
    private Integer scale;
    private Point point;
    
    private PlayerAvatarReturn setAvatarAttrs(int x, int y, int scale) {
        if (x < 0 || y < 0 || scale < 0) return PlayerAvatarReturn.NOT_OK;
        this.scale = scale;
        this.point = new Point(x, y);
        return PlayerAvatarReturn.OK;
    }
    
    private PlayerAvatarReturn setAvatar(BufferedImage img) {
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
        scale = null;
        point = null;
        DAO.save(avatar);
        return PlayerAvatarReturn.OK;
    }

    private static BufferedImage resize(BufferedImage img, int scale) {
        return Scalr.resize(img, Scalr.Method.QUALITY, scale, 0, Scalr.OP_ANTIALIAS);
    }
    
    private RenderedImage createDefaultAvatarImage(int scale) {
        try {
            return resize(ImageIO.read(Resource.class.getResource("avatar.png")), scale);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    private RenderedImage createAvatarImage(String user, int scale) {
        if (user == null) user = getPlayerName();
        if (user != null) {
            Player me = getPlayer();
            Player p = PDAO.getPlayer(user);
            if (p != null) {
                if (p == me || p.getFriendList().contains(me) || me.canUsePermission(p, Permission.SEE_EVERYONES_AVATAR)) {
                    PlayerAvatar avatar = DAO.getPlayerAvatar(user);
                    if (avatar != null) {
                        BufferedImage img = avatar.createAvatarImage();
                        if (img != null) return resize(img, scale);
                    }
                }
            }
        }
        return createDefaultAvatarImage(scale);
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
            if (action.equals(REQ_GET_AVATAR)) {
                try {
                    int scale = Integer.parseInt(rm.getFirst(KEY_SCALE));
                    if (scale < 0) scale = 1;
                    if (scale > 1000) scale = 1000;
                    return createAvatarImage(rm.getFirst(KEY_USER), scale);
                }
                catch (Exception ex) {
                    return getAvatarImage();
                }
            }
        }
        return null;
    }
    
    @Override
    protected int setImage(RenderedImage img, HttpServletRequest servletRequest, RequestMap request) {
        String action = request.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_SET_AVATAR)) return setAvatar((BufferedImage)img).ordinal();
        }
        return PlayerAvatarReturn.NOT_OK.ordinal();
    }
    
    @Override
    protected PlayerAvatarData getProperties(HttpServletRequest hsr, RequestMap rm) {
        PlayerAvatar a = getPlayerAvatar();
        if (a == null) return new PlayerAvatarData(getPlayerName());
        else return new PlayerAvatarData(getPlayerName(), a.getTopLeftPoint().getX(), a.getTopLeftPoint().getY(), a.getScale());
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
    
    public static void main(String[] args) throws IOException {
        BufferedImage img = ImageIO.read(new File("/home/zoli/google.png"));
        PlayerAvatarModel model = new PlayerAvatarModel() {

            @Override
            public Player getPlayer() {
                return new Player("fzoli", "", "");
            }
            
        };
        model.setAvatarAttrs(500, 160, 300);
        model.setAvatar(img);
    }
    
}