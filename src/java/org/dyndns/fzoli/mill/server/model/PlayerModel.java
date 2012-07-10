package org.dyndns.fzoli.mill.server.model;

import java.io.File;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.email.GMailSender;
import org.dyndns.fzoli.mill.common.InputValidator;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.key.PersonalDataType;
import org.dyndns.fzoli.mill.common.key.PlayerKeys;
import org.dyndns.fzoli.mill.common.key.PlayerReturn;
import org.dyndns.fzoli.mill.common.model.entity.BasePlayer;
import org.dyndns.fzoli.mill.common.model.entity.OnlineStatus;
import org.dyndns.fzoli.mill.common.model.entity.PlayerStatus;
import org.dyndns.fzoli.mill.common.model.entity.Sex;
import org.dyndns.fzoli.mill.common.model.pojo.BaseOnlinePojo;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerEvent;
import org.dyndns.fzoli.mill.common.permission.Permission;
import org.dyndns.fzoli.mill.common.permission.Permissions;
import org.dyndns.fzoli.mill.server.model.dao.PlayerAvatarDAO;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.dao.ValidatorDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mill.server.model.entity.PersonalData;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.entity.PlayerAvatar;
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
        RESIGN,
        KICK,
        NORMAL
    }
    
    private final static PlayerDAO DAO = new PlayerDAO();
    private final static ValidatorDAO VDAO = new ValidatorDAO();
    private final static PlayerAvatarDAO ADAO = new PlayerAvatarDAO();
    
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
        return !DAO.isEmailExists(email);
    }
    
    public PlayerReturn signIn(String name, String password, boolean hash) {
        PlayerReturn ret = DAO.verify(name, password, hash);
        if (ret == PlayerReturn.OK) {
            if (player != null) signOut(SignOutType.RESIGN);
            player = DAO.getPlayer(name);
            boolean unsuspend = player.getPlayerStatus().equals(PlayerStatus.SUSPENDED);
            if (unsuspend) player.setPlayerStatus(PlayerStatus.NORMAL);
            player.updateSignInDate();
            DAO.save(player);
            commonPlayer = ConvertUtil.createPlayer(this);
            if (unsuspend) callOnPlayerChanged(player, PlayerChangeType.UNSUSPEND);
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
                OnlineStatus ps = OnlineStatus.valueOf(state);
                OnlineStatus old = player.getOnlineStatus();
                if (!old.equals(ps)) {
                    player.setOnlineStatus(ps);
                    commonPlayer.setOnline(ps.equals(OnlineStatus.ONLINE));
                    DAO.save(player);
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
            DAO.save(player);
            return PlayerReturn.OK;
        }
        else {
            return PlayerReturn.NOT_OK;
        }
    }
    
    private PlayerReturn setEmail(HttpServletRequest hsr, String password, String email, boolean safe) {
        if (isRequestWrong(password, safe)) return getError(password, safe);
        if (DAO.isEmailExists(email)) return PlayerReturn.EMAIL_NOT_FREE;
        commonPlayer.setEmail(email);
        player.setEmail(email);
        player.setValidated(false);
        DAO.save(player);
        validateEmail(hsr, password, safe);
        return PlayerReturn.OK;
    }
    
    private PlayerReturn validateEmail(HttpServletRequest hsr, String password, boolean safe) {
        if (isRequestWrong(password, safe)) return getError(password, safe);
        if (player.isValidated()) return PlayerReturn.NO_CHANGE;
        if (player.getEmail().isEmpty()) return PlayerReturn.NULL;
        File config = MillControllerServlet.getEmailConfig(hsr);
        try {
            String key = InputValidator.md5Hex(player.getEmail() + new Date().getTime() + Math.random());
            GMailSender.sendEmail(config, player.getEmail(), ValidatorServlet.getEmailValidationSubject(hsr), ValidatorServlet.createValidationEmail(hsr, key, player));
            VDAO.setKey(player, key);
            removeCaptcha();
            return PlayerReturn.OK;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return PlayerReturn.ERROR;
        }
    }
    
    private PlayerReturn suspendAccount(String password, boolean safe) {
        if (isRequestWrong(password, safe)) return getError(password, safe);
        if (player.getPermissionMask(false) == Permissions.ROOT) return PlayerReturn.NO_CHANGE;
        player.setPersonalData(new PersonalData());
        player.setPlayerStatus(PlayerStatus.SUSPENDED);
        player.setOnlineStatus(OnlineStatus.ONLINE);
        player.setPermissionMask(0);
        DAO.save(player);
        PlayerAvatar avatar = ADAO.getPlayerAvatar(player.getPlayerName());
        if (avatar != null) {
            avatar.reset();
            ADAO.save(avatar);
        }
        callOnPlayerChanged(player, PlayerChangeType.SUSPEND);
        signOut(SignOutType.KICK);
        return PlayerReturn.OK;
    }
    
    private PlayerReturn setPersonalData(PersonalDataType request, String value) {
        if (value == null || player == null || request == null) return PlayerReturn.NULL;
        PersonalData data = player.getPersonalData();
        PlayerReturn ret = PlayerReturn.NOT_OK;
        try {
            switch(request) {
                case FIRST_NAME:
                    if (InputValidator.isNameValid(value)) {
                        data.setFirstName(value);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case LAST_NAME:
                    if (InputValidator.isNameValid(value)) {
                        data.setLastName(value);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case INVERSE_NAME:
                    data.setInverseName(Boolean.getBoolean(value));
                    ret = PlayerReturn.OK;
                    break;
                case BIRTH_DATE:
                    Date date = new Date(Long.parseLong(value));
                    Date now = new Date();
                    if (!(date.after(now) || Math.abs(date.getTime() - now.getTime()) > 150 * 365.24 * 24 * 60 * 60 * 1000)) {
                        data.setBirthDate(date);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case SEX:
                    data.setSex(Sex.valueOf(value));
                    ret = PlayerReturn.OK;
                    break;
                case COUNTRY:
                    //TODO
                    data.setCountry(value);
                    ret = PlayerReturn.OK;
                    break;
                case REGION:
                    //TODO
                    data.setRegion(value);
                    ret = PlayerReturn.OK;
                    break;
                case CITY:
                    //TODO
                    data.setCity(value);
                    ret = PlayerReturn.OK;
                    break;
            }
        }
        catch (Exception ex) {
            ;
        }
        if (ret.equals(PlayerReturn.OK)) commonPlayer = ConvertUtil.createPlayer(this);
        return ret;
    }
    
    private boolean isRequestWrong(String password, boolean safe) {
        return getError(password, safe) != null;
    }
    
    private PlayerReturn getError(String password, boolean safe) {
        if (!isCaptchaValidated()) return PlayerReturn.NOT_OK;
        if (player == null) return PlayerReturn.NULL;
        if (!InputValidator.isPasswordValid(password, safe)) return PlayerReturn.INVALID;
        if (!safe) password = InputValidator.md5Hex(password);
        if (!password.equals(player.getPassword())) return PlayerReturn.NOT_OK;
        return null;
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
            case SUSPEND:
                onSuspend(p);
                break;
            case UNSUSPEND:
                onUnsuspend(p);
                break;
            case AVATAR_CHANGE:
                onAvatarChange(p);
                break;
            case AVATAR_ENABLE:
                onAvatarEnabled(true);
                break;
            case AVATAR_DISABLE:
                onAvatarEnabled(false);
                break;
        }
    }
    
    public void onValidate(boolean add) {
        commonPlayer = ConvertUtil.createPlayer(this);
        addEvent(new PlayerEvent(commonPlayer, add ? PlayerEvent.PlayerEventType.VALIDATE : PlayerEvent.PlayerEventType.INVALIDATE));
    }
    
    private void onAvatarChange(Player p) {
        if (PlayerAvatarModel.isEventImportant(getPlayer(), p)) {
            addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), PlayerEvent.PlayerEventType.AVATAR_CHANGE));
        }
    }
    
    private void onSuspend(Player p) {
        addSuspendEvent(p, true);
        commonPlayer = ConvertUtil.createPlayer(this);
    }
    
    private void onUnsuspend(Player p) {
        commonPlayer = ConvertUtil.createPlayer(this);
        addSuspendEvent(p, false);
    }
    
    private void onAvatarEnabled(boolean enabled) {
        commonPlayer = ConvertUtil.createPlayer(this);
        addEvent(new PlayerEvent(commonPlayer, enabled ? PlayerEvent.PlayerEventType.AVATAR_ENABLE : PlayerEvent.PlayerEventType.AVATAR_DISABLE));
    }
    
    private void addSuspendEvent(Player p, boolean suspend) {
        if (findPlayer(p.getPlayerName()) != null) {
            if (!player.canUsePermission(p, Permission.SUSPENDED_PLAYER_DETECT)) {
                addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), suspend ? PlayerEvent.PlayerEventType.SUSPEND : PlayerEvent.PlayerEventType.UNSUSPEND));
            }
        }
    }
    
    private BasePlayer findPlayer(String playerName) {
        if (commonPlayer == null) return null;
        return findPlayer(playerName, commonPlayer.createMergedPlayerList());
    }
    
    private BasePlayer findPlayer(String playerName, List<BasePlayer> l) {
        for (BasePlayer bp : l) {
            if (bp.getPlayerName().equals(playerName)) {
                return bp;
            }
        }
        return null;
    }
    
    private PlayerData.PlayerList findPlayerList(String playerName) {
        if (commonPlayer != null) {
            if (findPlayer(playerName, commonPlayer.getBlockedUserList()) != null) return PlayerData.PlayerList.BLOCKED_PLAYERS;
            if (findPlayer(playerName, commonPlayer.getFriendWishList()) != null) return PlayerData.PlayerList.WISHED_FRIENDS;
            if (findPlayer(playerName, commonPlayer.getPossibleFriends()) != null) return PlayerData.PlayerList.POSSIBLE_FRIENDS;
        }
        return PlayerData.PlayerList.FRIENDS;
    }
    
    private void onSignInOut(Player p, boolean signIn, boolean sign) {
        if (player != null) {
            boolean canDetect = player.canUsePermission(p, Permission.INVISIBLE_STATUS_DETECT);
            if (sign && p.getOnlineStatus().equals(OnlineStatus.INVISIBLE) && !canDetect) return; //ha be/ki-jelentkezés van és láthatatlan és nincs láthatatlanság detektáló jog, akkor nem kell jelezni
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
            if (value != null) {
                if (action.equals(REQ_IS_EMAIL_FREE)) return isEmailFree(value) ? 1 : 0;
            }
            String passwd = rm.getFirst(KEY_PASSWORD);
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
            if (value != null) {
                try {
                    PersonalDataType request = PersonalDataType.valueOf(action);
                    setPersonalData(request, value);
                }
                catch (IllegalArgumentException ex) {
                    ;
                }
                if (action.equals(REQ_SET_ONLINE_STATUS)) return setPlayerState(value).ordinal();
//                if (action.equals(REQ_SET_FIRST_NAME)) return setPersonalData(PersonalDataType.FIRST_NAME, value).ordinal();
//                if (action.equals(REQ_SET_LAST_NAME)) return setPersonalData(PersonalDataType.LAST_NAME, value).ordinal();
//                if (action.equals(REQ_SET_INVERSE_NAME)) return setPersonalData(PersonalDataType.INVERSE_NAME, value).ordinal();
//                if (action.equals(REQ_SET_BIRTH_DATE)) return setPersonalData(PersonalDataType.BIRTH_DATE, value).ordinal();
//                if (action.equals(REQ_SET_SEX)) return setPersonalData(PersonalDataType.SEX, value).ordinal();
//                if (action.equals(REQ_SET_COUNTRY)) return setPersonalData(PersonalDataType.COUNTRY, value).ordinal();
//                if (action.equals(REQ_SET_REGION)) return setPersonalData(PersonalDataType.REGION, value).ordinal();
//                if (action.equals(REQ_SET_CITY)) return setPersonalData(PersonalDataType.CITY, value).ordinal();
                String passwd = rm.getFirst(KEY_PASSWORD);
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
        String user = rm.getFirst(KEY_USER);
        if (user != null) return new PlayerData(findPlayer(user), findPlayerList(user));
        return new PlayerData(commonPlayer, isCaptchaValidated(), getCaptchaWidth());
    }
    
    public void onDisconnect() {
        signOut(SignOutType.NORMAL);
    }
    
}