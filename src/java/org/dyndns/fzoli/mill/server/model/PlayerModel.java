package org.dyndns.fzoli.mill.server.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.email.GMailSender;
import org.dyndns.fzoli.location.entity.Location;
import org.dyndns.fzoli.mill.common.InputValidator;
import org.dyndns.fzoli.mill.common.Permission;
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
import org.dyndns.fzoli.mill.server.model.dao.CityDAO;
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

    private final static CityDAO CDAO = new CityDAO();
    private final static PlayerDAO DAO = new PlayerDAO();
    private final static ValidatorDAO VDAO = new ValidatorDAO();
    private final static PlayerAvatarDAO ADAO = new PlayerAvatarDAO();
    
    private Player player;
    private org.dyndns.fzoli.mill.common.model.entity.Player commonPlayer;

    private final HashMap<Player, PlayerChangeType> changedPlayers = new HashMap<Player, PlayerChangeType>();
    
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

    private PlayerReturn setActivePermission(int mask) {
        if (player != null && isCaptchaValidated()) {
            player.setActivePermissionMask(mask);
            DAO.save(player);
            return PlayerReturn.OK;
        }
        return PlayerReturn.NOT_OK;
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
        if (player.getPermissionMask(false) == Permission.ROOT) return PlayerReturn.NO_CHANGE;
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
        Player tmp = player;
        signOut(SignOutType.KICK);
        callOnPlayerChanged(tmp, PlayerChangeType.SUSPEND);
        return PlayerReturn.OK;
    }
    
    private PlayerReturn setPersonalData(PersonalDataType request, String value) {
        if (!isCaptchaValidated()) return PlayerReturn.NOT_OK;
        if (value == null || player == null || request == null) return PlayerReturn.NULL;
        PersonalData data = player.getPersonalData();
        PlayerReturn ret = PlayerReturn.NOT_OK;
        try {
            switch(request) {
                case FIRST_NAME:
                    if (InputValidator.isNameValid(value) && !value.equals(data.getFirstName())) {
                        data.setFirstName(value);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case LAST_NAME:
                    if (InputValidator.isNameValid(value) && !value.equals(data.getLastName())) {
                        data.setLastName(value);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case INVERSE_NAME:
                    boolean b = Boolean.parseBoolean(value);
                    if (b != data.isInverseName()) {
                        data.setInverseName(b);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case BIRTH_DATE:
                    Date date = new Date(Long.parseLong(value));
                    if (InputValidator.isBirthDateValid(date)) {
                        data.setBirthDate(date);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case SEX:
                    Sex s = Sex.valueOf(value);
                    if (!s.equals(data.getSex())) {
                        data.setSex(s);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case COUNTRY:
                    String country = data.getCountry();
                    if ((country == null || !country.equals(value)) && CDAO.getCountryByName(value) != null) {
                        data.setCountry(value);
                        data.setRegion(null);
                        data.setCity(null);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case REGION:
                    String region = data.getRegion();
                    country = data.getCountry();
                    if (country != null && (region == null || !region.equals(value)) && !CDAO.getRegions(country, value).isEmpty()) {
                        data.setRegion(value);
                        data.setCity(null);
                        ret = PlayerReturn.OK;
                    }
                    break;
                case CITY:
                    String city = data.getCity();
                    region = data.getRegion();
                    if (region != null && (city == null || !city.equals(value)) && !CDAO.getCities(data.getCountry(), region, value).isEmpty()) {
                        data.setCity(value);
                        ret = PlayerReturn.OK;
                    }
                    break;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (ret.equals(PlayerReturn.OK)) {
            commonPlayer = ConvertUtil.createPlayer(this);
            DAO.save(player);
            callOnPlayerChanged(player, PlayerChangeType.PERSONAL_DATA);
        }
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
                if (changedPlayers.containsKey(p)) {
                    synchronized(changedPlayers) {
                        switch (changedPlayers.get(p)) {
                            case PERSONAL_DATA:
                                onPersonalDataChanged(p);
                                break;
                            case AVATAR_CHANGE:
                                onAvatarChange(p);
                                break;
                        }
                        changedPlayers.remove(p);
                    }
                }
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
            case PERSONAL_DATA:
                onPersonalDataChanged(p);
                break;
        }
    }
    
    public static boolean isEventImportant(Player me, Player p) {
        if (me != null && me != p) {
            if ((p.getFriendList().contains(me) || me.canUsePermission(p, Permission.SEE_EVERYONES_AVATAR)) && (p.getOnlineStatus().equals(OnlineStatus.ONLINE) || me.canUsePermission(p, Permission.INVISIBLE_STATUS_DETECT))) {
                return true;
            }
        }
        return false;
    }
    
    public void onValidate(boolean add) {
        commonPlayer = ConvertUtil.createPlayer(this);
        addEvent(new PlayerEvent(commonPlayer, add ? PlayerEvent.PlayerEventType.VALIDATE : PlayerEvent.PlayerEventType.INVALIDATE));
    }
    
    private void onPersonalDataChanged(Player p) {
        if (player != p) {
            if (isEventImportant(player, p)) {
                commonPlayer = ConvertUtil.createPlayer(this);
                addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), PlayerEvent.PlayerEventType.PERSONAL_DATA_CHANGE));
            }
            else {
                synchronized(changedPlayers) {
                    changedPlayers.put(p, PlayerChangeType.PERSONAL_DATA);
                }
            }
        }
    }
    
    private void onAvatarChange(Player p) {
        if (isEventImportant(getPlayer(), p)) {
            addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), PlayerEvent.PlayerEventType.AVATAR_CHANGE));
        }
        else {
            synchronized(changedPlayers) {
                changedPlayers.put(p, PlayerChangeType.AVATAR_CHANGE);
            }
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
    
    public void onDisconnect() {
        signOut(SignOutType.NORMAL);
    }
    
    private void addSuspendEvent(Player p, boolean suspend) {
        if (findPlayer(p.getPlayerName()) != null) {
            if (!player.canUsePermission(p, Permission.SUSPENDED_PLAYER_DETECT)) {
                addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), suspend ? PlayerEvent.PlayerEventType.SUSPEND : PlayerEvent.PlayerEventType.UNSUSPEND));
            }
            else {
                addEvent(new PlayerEvent(commonPlayer, p.getPlayerName(), PlayerEvent.PlayerEventType.RELOAD));
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
                    return setPersonalData(request, value).ordinal();
                }
                catch (IllegalArgumentException ex) {
                    ;
                }
                try {
                    if (action.equals(REQ_SET_ACTIVE_PERMISSION)) return setActivePermission(Integer.parseInt(value)).ordinal();
                }
                catch (NumberFormatException ex) {
                    ;
                }
                if (action.equals(REQ_SET_ONLINE_STATUS)) return setPlayerState(value).ordinal();
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
        if (user != null && player != null) {
            if (user.equals(player.getPlayerName())) return new PlayerData(commonPlayer, null);
            else return new PlayerData(findPlayer(user), findPlayerList(user));
        }
        String value = rm.getFirst(KEY_VALUE);
        String action = rm.getFirst(KEY_REQUEST);
        if (player != null && value != null && action != null) {
            PersonalData data = player.getPersonalData();
            if (action.equals(REQ_GET_COUNTRIES)) return new PlayerData(createList(CDAO.findCountriesByName(value)));
            if (action.equals(REQ_GET_REGIONS)) return new PlayerData(createList(CDAO.findRegions(data.getCountry(), value)));
            if (action.equals(REQ_GET_CITIES)) return new PlayerData(createList(CDAO.findCities(data.getCountry(), data.getRegion(), value)));
        }
        return new PlayerData(commonPlayer, isCaptchaValidated(), getCaptchaWidth());
    }
    
    private static List<String> createList(List<? extends Location> l) {
        List<String> ls = new ArrayList<String>();
        for (Location o : l) {
            ls.add(o.getDisplay());
        }
        return ls;
    }
    
}