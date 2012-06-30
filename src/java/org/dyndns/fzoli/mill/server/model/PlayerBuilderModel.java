package org.dyndns.fzoli.mill.server.model;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.InputValidator;
import org.dyndns.fzoli.mill.common.key.PlayerBuilderKeys;
import org.dyndns.fzoli.mill.common.key.PlayerBuilderReturn;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerBuilderData;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerBuilderEvent;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class PlayerBuilderModel extends AbstractMillModel<PlayerBuilderEvent, PlayerBuilderData> implements PlayerBuilderKeys {
    
    private final static int TIMEOUT = 600000; // 10 minutes
    private long initTime = new Date().getTime();
    private String user = "", email = "";
    
    public List<PlayerBuilderModel> findModels() {
        return findModels(getKey(), PlayerBuilderModel.class);
    }
    
    public boolean isTimeout(long time) {
        return time - initTime >= TIMEOUT;
    }
    
    public boolean isUserFree(String user) {
        if (user == null) return true;
        if (PlayerDAO.isPlayerNameExists(user)) return false;
        List<PlayerBuilderModel> models = findModels();
        long time = new Date().getTime();
        for (PlayerBuilderModel model : models) {
            if (!model.isTimeout(time) && user.equals(model.user)) return false;
        }
        return true;
    }
    
    public boolean isEmailFree(String email) {
        return !PlayerDAO.isEmailExists(email);
    }

    public PlayerBuilderReturn setUser(String user) {
        if (user.equals(this.user)) return PlayerBuilderReturn.OK;
        if (InputValidator.isUserIdValid(user)) {
            if (!isUserFree(user)) return PlayerBuilderReturn.USER_EXISTS;
            this.user = user;
            return PlayerBuilderReturn.OK;
        }
        return PlayerBuilderReturn.INVALID_USER;
    }
    
    public PlayerBuilderReturn setEmail(String email) {
        if (email.equals(this.email)) return PlayerBuilderReturn.OK;
        if (InputValidator.isEmailValid(email)) {
            if (!isEmailFree(email)) return PlayerBuilderReturn.EMAIL_EXISTS;
            this.email = email;
            return PlayerBuilderReturn.OK;
        }
        return PlayerBuilderReturn.INVALID_EMAIL;
    }
    
    public PlayerBuilderReturn createUser(String password, boolean hash) {
        if (!isCaptchaValidated()) return PlayerBuilderReturn.WRONG_CAPTCHA;
        if (validate(false)) return PlayerBuilderReturn.VALIDATED;
        if (InputValidator.isPasswordValid(password, hash) && InputValidator.isUserIdValid(user) && InputValidator.isEmailValid(email)) {
            PlayerBuilderReturn ret = PlayerDAO.createPlayer(new Player(user, password, email), hash);
            if (ret == PlayerBuilderReturn.OK) {
                addStaticEvent();
                validate(true);
                removeCaptcha();
                getPlayerModel(true).signIn(user, password, hash);
            }
            return ret;
        }
        return PlayerBuilderReturn.INVALID;
    }
    
    @Override
    protected int askModel(HttpServletRequest sr, RequestMap m) {
        String action = m.getFirst(KEY_REQUEST);
        String value = m.getFirst(KEY_VALUE);
        if (action != null) {
            if (action.equals(REQ_VALIDATE)) return (validate(true) ? PlayerBuilderReturn.VALIDATED : PlayerBuilderReturn.NOT_VALIDATED).ordinal();
            if (action.equals(REQ_CREATE)) return createUser(value, false).ordinal();
            if (action.equals(REQ_SAFE_CREATE)) return createUser(value, true).ordinal();
        }
        return super.askModel(sr, m);
    }
    
    @Override
    protected PlayerBuilderData getProperties(HttpServletRequest sr, RequestMap m) {
        validate(false);
        return new PlayerBuilderData(user, email, new Date().getTime(), initTime, TIMEOUT, PlayerDAO.getPlayerCount(), isCaptchaValidated(), getCaptchaWidth());
    }

    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        String value = rm.getFirst(KEY_VALUE);
        if (action != null && value != null) {
            if (action.equals(REQ_SET_USER)) return setUser(value).ordinal();
            if (action.equals(REQ_SET_EMAIL)) return setEmail(value).ordinal();
        }
        return PlayerBuilderReturn.NULL.ordinal();
    }
    
    private boolean validate(boolean force) {
        long time = new Date().getTime();
        if (force || isTimeout(time)) {
            user = "";
            email = "";
            initTime = time;
            addEvent(true);
            return true;
        }
        return false;
    }
    
    private void addEvent(boolean reset) {
        addEvent(new PlayerBuilderEvent(reset, PlayerDAO.getPlayerCount()));
    }

    private void addStaticEvent() {
        addStaticEvent(new PlayerBuilderEvent(false, PlayerDAO.getPlayerCount()));
    }
    
}