package org.dyndns.fzoli.mill.server.model.map;

import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.server.model.PlayerAvatarModel;
import org.dyndns.fzoli.mill.server.model.PlayerBuilderModel;
import org.dyndns.fzoli.mill.server.model.PlayerModel;
import org.dyndns.fzoli.mvc.server.model.JSONModel;
import org.dyndns.fzoli.mvc.server.model.annotation.UseModelMap;
import org.dyndns.fzoli.mvc.server.model.map.ModelMap;

/**
 *
 * @author zoli
 */
@UseModelMap
public final class MillModelMap extends ModelMap<JSONModel> implements ModelKeys {
    
    public MillModelMap() {
        super();
    }

    @Override
    protected JSONModel init(String key) {
        if (key.equals(PLAYER_BUILDER)) return new PlayerBuilderModel();
        if (key.equals(PLAYER)) return new PlayerModel();
        if (key.equals(PLAYER_AVATAR)) return new PlayerAvatarModel();
        return super.init(key);
    }
    
}