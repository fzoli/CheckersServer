package org.dyndns.fzoli.mill.server.model;

import java.io.File;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.email.GMailSender;
import org.dyndns.fzoli.mill.common.InputValidator;
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
import org.dyndns.fzoli.mill.server.model.dao.ValidatorDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.servlet.MillControllerServlet;
import org.dyndns.fzoli.mill.server.servlet.ValidatorServlet;
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
    
    private PlayerReturn setPassword(String oldPassword, String newPassword, boolean safe) {
        if (!isCaptchaValidated()) return PlayerReturn.NOT_OK;
        if (player == null) return PlayerReturn.NULL;
        if (!InputValidator.isPasswordValid(newPassword, safe) || !InputValidator.isPasswordValid(oldPassword, safe)) return PlayerReturn.INVALID;
        if (!safe) {
            oldPassword = InputValidator.md5Hex(oldPassword);
            newPassword = InputValidator.md5Hex(newPassword);
        }
        if (oldPassword.equals(newPassword)) {
            return PlayerReturn.NO_CHANGE;
        }
        if (oldPassword.equals(player.getPassword())) {
            player.setPassword(newPassword);
            PlayerDAO.save(player);
            return PlayerReturn.OK;
        }
        else {
            return PlayerReturn.NOT_OK;
        }
    }
    
    private PlayerReturn setEmail(HttpServletRequest hsr, String password, String email, boolean safe) {
        if (!isCaptchaValidated()) return PlayerReturn.NOT_OK;
        if (player == null) return PlayerReturn.NULL;
        if (!InputValidator.isPasswordValid(password, safe)) return PlayerReturn.INVALID;
        if (!safe) password = InputValidator.md5Hex(password);
        if (password.equals(player.getPassword())) {
            if (PlayerDAO.isEmailExists(email)) return PlayerReturn.EMAIL_NOT_FREE;
            commonPlayer.setEmail(email);
            player.setEmail(email);
            player.setValidated(false);
            PlayerDAO.save(player);
            validateEmail(hsr, password, true);
            return PlayerReturn.OK;
        }
        return PlayerReturn.NOT_OK;
    }
    
    private PlayerReturn validateEmail(HttpServletRequest hsr, String password, boolean safe) {
        if (!isCaptchaValidated()) return PlayerReturn.NOT_OK;
        if (player == null || player.getEmail().isEmpty()) return PlayerReturn.NULL;
        if (player.isValidated()) return PlayerReturn.NO_CHANGE;
        if (!InputValidator.isPasswordValid(password, safe)) return PlayerReturn.INVALID;
        if (!safe) password = InputValidator.md5Hex(password);
        if (!password.equals(player.getPassword())) return PlayerReturn.NOT_OK;
        String host = MillControllerServlet.getHost(hsr);
        File config = MillControllerServlet.getEmailConfig(hsr);
        try {
            String key = InputValidator.md5Hex(player.getEmail() + new Date().getTime() + Math.random());
            GMailSender.sendEmail(config, player.getEmail(), ValidatorServlet.getEmailValidationSubject(hsr), ValidatorServlet.createValidationEmail(hsr, key, player));
            ValidatorDAO.setKey(player, key);
            removeCaptcha();
            return PlayerReturn.OK;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return PlayerReturn.ERROR;
        }
    }
    
    private PlayerReturn suspendAccount(String password, boolean safe) { //TODO
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
    protected int askModel(HttpServletRequest hsr, RequestMap rm) {
        int ret = super.askModel(hsr, rm);
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_SIGN_OUT)) return signOut(SignOutType.NORMAL).ordinal();
            if (action.equals(REQ_SIGN_IN)) return signIn(rm.getFirst(KEY_USER), rm.getFirst(KEY_PASSWORD), false).ordinal();
            if (action.equals(REQ_SAFE_SIGN_IN)) return signIn(rm.getFirst(KEY_USER), rm.getFirst(KEY_PASSWORD), true).ordinal();
            String value = rm.getFirst(KEY_VALUE);
            String passwd = rm.getFirst(KEY_PASSWORD);
            if (value != null) {
                if (action.equals(REQ_IS_EMAIL_FREE)) return isEmailFree(value) ? 1 : 0;
            }
            if (passwd != null) {
                if (action.equals(REQ_REVALIDATE_EMAIL)) return validateEmail(hsr, passwd, false).ordinal();
                if (action.equals(REQ_SAFE_REVALIDATE_EMAIL)) return validateEmail(hsr, passwd, true).ordinal();
                if (action.equals(REQ_SUSPEND_ACCOUNT)) return suspendAccount(passwd, false).ordinal();
                if (action.equals(REQ_SAFE_SUSPEND_ACCOUNT)) return suspendAccount(passwd, true).ordinal();
            }
        }
        return ret == -1 ? PlayerReturn.NULL.ordinal() : ret;
    }
    
    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            String value = rm.getFirst(KEY_VALUE);
            String passwd = rm.getFirst(KEY_PASSWORD);
            if (value != null) {
                if (action.equals(REQ_SET_PLAYER_STATE)) return setPlayerState(value).ordinal();
                if (passwd != null) {
                    if (action.equals(REQ_SET_EMAIL)) return setEmail(hsr, passwd, value, false).ordinal();
                    if (action.equals(REQ_SAFE_SET_EMAIL)) return setEmail(hsr, passwd, value, true).ordinal();
                    if (action.equals(REQ_SET_PASSWORD)) return setPassword(passwd, value, false).ordinal();
                    if (action.equals(REQ_SET_SAFE_PASSWORD)) return setPassword(passwd, value, true).ordinal();
                }
            }
        }
        return PlayerReturn.NULL.ordinal(); 
    }

    @Override
    protected PlayerData getProperties(HttpServletRequest hsr, RequestMap rm) {
        return new PlayerData(commonPlayer, isCaptchaValidated(), getCaptchaWidth());
    }
    
    public void onDisconnect() {
        signOut(SignOutType.NORMAL);
    }
    
}