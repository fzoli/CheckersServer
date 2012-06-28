package org.dyndns.fzoli.mill.server.model;

import java.util.List;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.model.entity.PlayerState;
import org.dyndns.fzoli.mill.common.model.pojo.BaseOnlinePojo;
import org.dyndns.fzoli.mill.common.permission.Permission;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mvc.server.model.Model;
import org.dyndns.fzoli.mvc.server.model.bean.ModelBean;
import org.dyndns.fzoli.mvc.server.model.bean.ModelBeanRegister;

/**
 *
 * @author zoli
 */
public abstract class AbstractOnlineModel<EventObj extends BaseOnlinePojo, PropsObj extends BaseOnlinePojo> extends AbstractMillModel<EventObj, PropsObj> {

    protected enum PlayerChangeType {
        SIGN_IN,
        SIGN_OUT,
        STATE_INVISIBLE,
        STATE_ONLINE
    }
    
    public boolean isOnline(Player p) {
        Player player = getPlayer();
        if (player.equals(p)) return player.getPlayerState().equals(PlayerState.ONLINE);
        if (!player.canUsePermission(p, Permission.INVISIBLE_STATUS_DETECT)) {
            if (!player.getFriendList().contains(p)) return false;
            if (p.getPlayerState().equals(PlayerState.INVISIBLE)) return false;
        }
        List<ModelBean> beans = ModelBeanRegister.getModelBeans();
        synchronized (beans) {
            for (ModelBean bean : beans) {
                try {
                    PlayerModel pm = (PlayerModel) bean.getModel(ModelKeys.PLAYER);
                    if (p.getPlayerName().equals(pm.getPlayer().getPlayerName())) return true;
                }
                catch (NullPointerException ex) {
                    ;
                }
            }
        }
        return false;
    }
    
    @Override
    public void addEvent(EventObj eo) {
        super.addEvent(eo);
    }
    
    protected void onPlayerChanged(Player p, PlayerChangeType type) {
        ;
    }
    
    protected void callOnPlayerChanged(final Player p, final PlayerChangeType type) {
        iterateEveryModel(new ModelIterator() {

            @Override
            public void handler(String string, Model model) {
                if (model instanceof AbstractOnlineModel) {
                        ((AbstractOnlineModel)model).onPlayerChanged(p, type);
                }
            }

        });
    }
    
}