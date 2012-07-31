package org.dyndns.fzoli.mill.server.model;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
        if (me != null) setParams(names, age, sexName, country, region, city);
        return ConvertUtil.createPlayerList(this, DAO.getPlayers(page, me, names, age, sexName, country, region, city));
    }
    
    @Override
    protected PlayerRegistryData getProperties(HttpServletRequest hsr, RequestMap rm) {
        return new PlayerRegistryData(getPlayerName());
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }
    
    private void setParams(String names, String age, String sexName, String country, String region, String city) {
        this.names = names;
        this.age = age;
        this.sexName = sexName;
        this.country = country;
        this.region = region;
        this.city = city;
    }
    
}