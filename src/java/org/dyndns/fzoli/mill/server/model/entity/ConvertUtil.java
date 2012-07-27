package org.dyndns.fzoli.mill.server.model.entity;

import java.util.ArrayList;
import java.util.List;
import org.dyndns.fzoli.mill.common.model.entity.BasePlayer;
import org.dyndns.fzoli.mill.common.model.entity.Message;
import org.dyndns.fzoli.mill.common.model.entity.PersonalData;
import org.dyndns.fzoli.mill.common.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.AbstractOnlineModel;

/**
 *
 * @author zoli
 */
public class ConvertUtil {
    
    public static Player createPlayer(AbstractOnlineModel model) {
        if (model == null) return null;
        return createPlayer(model, model.getPlayer());
    }
    
    public static Player createPlayer(AbstractOnlineModel m, org.dyndns.fzoli.mill.server.model.entity.Player p) {
        if (p == null || m == null) return null;
        return new Player(p.getPlayerName(), p.getEmail(), p.isValidated(), p.isAvatarEnabled(), p.getPermissionMask(false), p.getPermissionMask(true), p.getSignUpDate(), p.getSignInDate(), createPersonalData(p.getPersonalData()), p.isSuspended(), createPlayerList(m, p.getFriendList()), createPlayerList(m, p.getFriendWishList()), createPlayerList(m, p.getBlockedUserList()), createPlayerList(m, p.getPossibleFriends()), m.isOnline(p));
    }
    
    public static BasePlayer createBasePlayer(AbstractOnlineModel m, org.dyndns.fzoli.mill.server.model.entity.Player p) {
        if (p == null || m == null) return null;
        return new BasePlayer(p.getPlayerName(), p.getSignUpDate(), p.getSignInDate(), createPersonalData(p.getPersonalData()), p.isSuspended(), m.isOnline(p));
    }
    
    public static List<BasePlayer> createPlayerList(AbstractOnlineModel model, List<org.dyndns.fzoli.mill.server.model.entity.Player> l) {
        if (l == null) return null;
        ArrayList<BasePlayer> pl = new ArrayList<BasePlayer>();
        for (org.dyndns.fzoli.mill.server.model.entity.Player p : l) {
            pl.add(createBasePlayer(model, p));
        }
        return pl;
    }
    
    public static PersonalData createPersonalData(org.dyndns.fzoli.mill.server.model.entity.PersonalData d) {
        if (d == null) return null;
        return new PersonalData(d.getFirstName(), d.getLastName(), d.isInverseName(), d.getBirthDate(), d.getSex(), d.getCountry(), d.getRegion(), d.getCity());
    }
    
    public static List<Message> createMessageList(List<org.dyndns.fzoli.mill.server.model.entity.Message> l) {
        List<Message> ls = new ArrayList<Message>();
        if (l != null) {
            for (org.dyndns.fzoli.mill.server.model.entity.Message m : l) {
                ls.add(createMessage(m));
            }
        }
        return ls;
    }
    
    public static Message createMessage(org.dyndns.fzoli.mill.server.model.entity.Message m) {
        return createMessage(m, null);
    }
    
    private static Message createMessage(org.dyndns.fzoli.mill.server.model.entity.Message m, String defSender) {
        if (m == null) return null;
        return new Message(m.getAddress().getPlayerName(), m.getSender() == null ? defSender : m.getSender().getPlayerName(), m.getText(), m.getSendDate(), m.getType(), m.getSystemMessage());
    }
    
}