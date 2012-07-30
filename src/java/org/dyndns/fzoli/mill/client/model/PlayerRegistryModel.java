package org.dyndns.fzoli.mill.client.model;

import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.key.PlayerRegistryKeys;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerRegistryEvent;
import org.dyndns.fzoli.mvc.client.connection.Connection;

/**
 *
 * @author zoli
 */
public class PlayerRegistryModel extends AbstractOnlineModel<PlayerRegistryEvent, PlayerRegistryData> implements PlayerRegistryKeys {

    public PlayerRegistryModel(Connection<Object, Object> connection) {
        super(connection, ModelKeys.PLAYER_REGISTRY, PlayerRegistryEvent.class, PlayerRegistryData.class);
    }
    
}