package org.dyndns.fzoli.mill.server.model;

import java.util.List;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.key.PlayerKeys;
import org.dyndns.fzoli.mill.common.key.PlayerReturn;
import org.dyndns.fzoli.mill.common.model.entity.BasePlayer;
import org.dyndns.fzoli.mill.common.model.entity.PlayerState;
import org.dyndns.fzoli.mill.common.model.pojo.BaseOnlinePojo;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerEvent;
import org.dyndns.fzoli.mill.common.permission.Permission;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;
import org.dyndns.fzoli.mvc.server.model.Model;

/**
 *
 * @author zoli
 */
public class PlayerModel extends AbstractOnlineModel<PlayerEvent, PlayerData> implements PlayerKeys {
    
    public static enum SignOutType {
        RESIGN, KICK, NORMAL
    }
    
    private final PlayerDAO DAO = new PlayerDAO(this);
    
    private Player player;
    private org.dyndns.fzoli.mill.common.model.entity.Player commonPlayer;

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public org.dyndns.fzoli.mill.common.model.entity.Player getCommonPlayer() {
        return commonPlayer;
    }

    public boolean isEmailFree(String email) {
        return !PlayerDAO.isEmailExists(email);
    }
    
    public PlayerReturn signIn(String name, String password, boolean hash) {
        System.out.println(name + " ; " + password + " ; " + hash);
        PlayerReturn ret = PlayerDAO.verify(name, password, hash);
        if (ret == PlayerReturn.OK) {
            if (player != null) signOut(SignOutType.RESIGN);
            player = PlayerDAO.getPlayer(name);
            player.updateSignInDate();
            PlayerDAO.save(player);
            commonPlayer = ConvertUtil.createPlayer(this);
            getModelMap().remove(ModelKeys.PLAYER_BUILDER);
            boolean wasKick = false;
            List<PlayerModel> models = findModels(getKey(), PlayerModel.class);
            for (PlayerModel model : models) {
                String n = model.getPlayerName();
                if (n == null || model == this) continue;
                if (n.equals(name)) { // ha másik sessionben be van már lépve, kijelentkeztetés
                    model.signOut(SignOutType.KICK);
                    wasKick = true;
                    break;
                }
            }
            if (!wasKick) { // bejelentkezés jelzés ha nem volt már bejelentkezve
                callOnPlayerChanged(player, PlayerChangeType.SIGN_IN);
            }
        }
        else {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException ex) {
                ;
            }
        }
        return ret;
    }
    
    public PlayerReturn signOut(SignOutType type) {
        if (player == null) return PlayerReturn.NULL;
        if (type != SignOutType.KICK) { // ha nem történt másik sessionben bejelentkezés
            callOnPlayerChanged(player, PlayerChangeType.SIGN_OUT);
        }
        player = null;
        commonPlayer = null;
        if (type == SignOutType.KICK) { // ha másik sessionben bejelentkeztek
            iterateBeanModels(new ModelIterator() {

                @Override
                public void handler(String key, Model model) {
                    if (model instanceof AbstractOnlineModel) { // a session minden online modeljének jelzés a kijelentkeztetésről
                        ((AbstractOnlineModel)model).addEvent(new BaseOnlinePojo(commonPlayer));
                    }
                }
                
            });
        }
        createCaptcha();
        return PlayerReturn.OK;
    }

    private PlayerReturn setPlayerState(String state) {
        if (player != null) {
            try {
                PlayerState ps = PlayerState.valueOf(state);
                PlayerState old = player.getPlayerState();
                if (!old.equals(ps)) {
                    player.setPlayerState(ps);
                    commonPlayer.setOnline(ps.equals(PlayerState.ONLINE));
                    PlayerDAO.save(player);
                    callOnPlayerChanged(player, commonPlayer.isOnline() ? PlayerChangeType.STATE_ONLINE : PlayerChangeType.STATE_INVISIBLE);
                    return PlayerReturn.OK;
                }
            }
            catch (IllegalArgumentException ex) {
                return PlayerReturn.INVALID;
            }
        }
        return PlayerReturn.NULL;
    }
    
    @Override
    public void onPlayerChanged(Player p, PlayerChangeType type) {
        super.onPlayerChanged(p, type);
        switch (type) {
            case SIGN_IN:
                onSignInOut(p, true, true);
                break;
            case SIGN_OUT:
                onSignInOut(p, false, true);
                break;
            case STATE_INVISIBLE:
                onSignInOut(p, false, false);
                break;
            case STATE_ONLINE:
                onSignInOut(p, true, false);
                break;
                
        }
    }
    
    private void onSignInOut(Player p, boolean signIn, boolean sign) {
        if (player != null) {
            boolean canDetect = player.canUsePermission(p, Permission.INVISIBLE_STATUS_DETECT);
            if (sign && p.getPlayerState().equals(PlayerState.INVISIBLE) && !canDetect) return; //ha be/ki-jelentkezés van és láthatatlan és nincs láthatatlanság detektáló jog, akkor nem kell jelezni
            if (!sign && canDetect) return; // ha állapot váltás történt (tehát nem be/ki-jelentkezés) és van láthatatlanság detektáló jog, nem kell jelezni
            System.out.print("sign " + p.getPlayerName() + " " + (signIn ? "in" : "out") + " detected on session of " + player.getPlayerName() + "...");
            if (player.isFriend(p)) {
                System.out.println("sent.");
                BasePlayer bp = commonPlayer.findPlayer(p.getPlayerName());
                bp.setOnline(signIn);
                addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), signIn));
            }
            else {
                System.out.println("not sent.");
            }
        }
    }
    
    @Override
    protected int askModel(RequestMap rm) {
        int ret = super.askModel(rm);
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_SIGN_OUT)) return signOut(SignOutType.NORMAL).ordinal();
            if (action.equals(REQ_SIGN_IN)) return signIn(rm.getFirst(KEY_USER), rm.getFirst(KEY_PASSWORD), false).ordinal();
            if (action.equals(REQ_SAFE_SIGN_IN)) return signIn(rm.getFirst(KEY_USER), rm.getFirst(KEY_PASSWORD), true).ordinal();
            String value = rm.getFirst(KEY_VALUE);
            if (value != null) {
                if (action.equals(REQ_IS_EMAIL_FREE)) return isEmailFree(value) ? 1 : 0;
            }
        }
        return ret == -1 ? PlayerReturn.NULL.ordinal() : ret;
    }
    
    @Override
    protected int setProperty(RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            String value = rm.getFirst(KEY_VALUE);
            if (value != null) {
                if (action.equals(REQ_SET_PLAYER_STATE)) return setPlayerState(value).ordinal();
            }
        }
        return PlayerReturn.NULL.ordinal(); 
    }

    @Override
    protected PlayerData getProperties(RequestMap rm) {
        return new PlayerData(commonPlayer, isCaptchaValidated(), getCaptchaWidth());
    }
    
    public void onDisconnect() {
        signOut(SignOutType.NORMAL);
    }
    
}