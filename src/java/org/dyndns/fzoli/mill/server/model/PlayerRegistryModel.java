package org.dyndns.fzoli.mill.server.model;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.Permission;
import org.dyndns.fzoli.mill.common.key.PlayerRegistryKeys;
import org.dyndns.fzoli.mill.common.model.entity.BasePlayer;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryEvent;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerRegistryModel extends AbstractOnlineModel<PlayerRegistryEvent, PlayerRegistryData> implements PlayerRegistryKeys {
    
    private final static PlayerDAO DAO = new PlayerDAO();
    
    private int page = 1;
    private String names, age, sexName, country, region, city;
    
    public List<BasePlayer> findPlayers(String names, String age, String sexName, String country, String region, String city) {
        Player me = getPlayer();
        List<Player> l;
        if (me != null) {
            l = DAO.getPlayers(page, names, age, sexName, country, region, city);
        }
        else {
            l = new ArrayList<Player>();
        }
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).canUsePermission(me, Permission.HIDDEN_MODE)) {
                l.remove(i);
                i--;
            }
        }
        return ConvertUtil.createPlayerList(this, l);
    }
    
    @Override
    protected PlayerRegistryData getProperties(HttpServletRequest hsr, RequestMap rm) {
        return new PlayerRegistryData(getPlayerName());
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }
    
}