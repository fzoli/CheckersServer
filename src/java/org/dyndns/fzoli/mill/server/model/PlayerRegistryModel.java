package org.dyndns.fzoli.mill.server.model;

import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.PlayerRegistryKeys;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryEvent;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerRegistryModel extends AbstractOnlineModel<PlayerRegistryEvent, PlayerRegistryData> implements PlayerRegistryKeys {
    
    private final static PlayerDAO DAO = new PlayerDAO();
    
    private int page = 1;
    private String names, age, sexName, country, region, city;
    
    private void setParams(String names, String age, String sexName, String country, String region, String city) {
        this.page = 1;
        this.names = names;
        this.age = age;
        this.sexName = sexName;
        this.country = country;
        this.region = region;
        this.city = city;
    }
    
    private PlayerRegistryData findPlayers() {
        return new PlayerRegistryData(getPlayerName(), ConvertUtil.createPlayerList(this, DAO.getPlayers(page, getPlayer(), names, age, sexName, country, region, city)), DAO.getPlayerCount(getPlayer(), names, age, sexName, country, region, city), page);
    }
    
    @Override
    protected PlayerRegistryData getProperties(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null && action.equals(REQ_FIND_PLAYERS)) return findPlayers();
        return new PlayerRegistryData(getPlayerName(), names, age, sexName, country, region, city);
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }
    
}