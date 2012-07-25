package org.dyndns.fzoli.mill.server.model;

import java.util.List;
import org.dyndns.fzoli.mill.common.Permission;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.model.entity.OnlineStatus;
import org.dyndns.fzoli.mill.common.model.pojo.BaseOnlinePojo;
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
        STATE_ONLINE,
        SUSPEND,
        UNSUSPEND,
        AVATAR_CHANGE,
        AVATAR_ENABLE,
        AVATAR_DISABLE,
        PERSONAL_DATA,
        NEW_MESSAGE
    }
    
    public boolean isOnline(Player p) {
        Player player = getPlayer();
        if (player.equals(p)) return player.getOnlineStatus().equals(OnlineStatus.ONLINE);
        if (!player.canUsePermission(p, Permission.DETECT_INVISIBLE_STATUS)) {
            if (!p.getFriendList().contains(player)) return false;
            if (p.getOnlineStatus().equals(OnlineStatus.INVISIBLE)) return false;
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
    
    protected void onPlayerChanged(EventObj evt) {
        ;
    }
    
    protected void callOnPlayerChanged(final Class clazz, final EventObj evt) {
        iterateEveryModel(new ModelIterator() {

            @Override
            public void handler(String string, Model model) {
                if (model instanceof AbstractOnlineModel && model.getClass().equals(clazz)) {
                    ((AbstractOnlineModel)model).onPlayerChanged(evt);
                }
            }

        });
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