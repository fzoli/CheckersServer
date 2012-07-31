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
    private String names, ages, sexName, country, region, city;
    
    private void setPage(String action, String value) {
        if (action.equals(REQ_GET_PAGE) && value != null) {
            try {
                page = Integer.parseInt(value);
            }
            catch (NumberFormatException ex) {
                page = 1;
            }
        }
        if (action.equals(REQ_PREV_PAGE)) {
            page--;
        }
        if (action.equals(REQ_NEXT_PAGE)) {
            page++;
        }
    }
    
    private PlayerRegistryData findPlayers() {
        long count = DAO.getPlayerCount(getPlayer(), names, ages, sexName, country, region, city);
        int lastPage = DAO.getLastPage(count);
        if (page < 1) page = 1;
        if (page > lastPage) page = lastPage;
        return new PlayerRegistryData(getPlayerName(), ConvertUtil.createPlayerList(this, DAO.getPlayers(page, getPlayer(), names, ages, sexName, country, region, city)), count, page, lastPage);
    }
    
    @Override
    protected PlayerRegistryData getProperties(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null && (action.equals(REQ_NEXT_PAGE) || action.equals(REQ_PREV_PAGE) || action.equals(REQ_GET_PAGE))) {
            setPage(action, rm.getFirst(KEY_VALUE));
            return findPlayers();
        }
        return new PlayerRegistryData(getPlayerName(), names, ages, sexName, country, region, city);
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_SET_FILTER)) {
                names = rm.getFirst(KEY_NAMES);
                ages = rm.getFirst(KEY_AGES);
                sexName = rm.getFirst(KEY_SEX);
                country = rm.getFirst(KEY_COUNTRY);
                region = rm.getFirst(KEY_REGION);
                city = rm.getFirst(KEY_CITY);
                return 1;
            }
        }
        return 0;
    }
    
}