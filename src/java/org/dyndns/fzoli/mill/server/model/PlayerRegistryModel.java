package org.dyndns.fzoli.mill.server.model;

import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.PlayerRegistryKeys;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryEvent;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerRegistryModel extends AbstractOnlineModel<PlayerRegistryEvent, PlayerRegistryData> implements PlayerRegistryKeys {

    @Override
    protected PlayerRegistryData getProperties(HttpServletRequest hsr, RequestMap rm) {
        return new PlayerRegistryData(getPlayerName());
    }

    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }
    
}