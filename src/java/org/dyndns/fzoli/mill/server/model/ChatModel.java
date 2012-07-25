package org.dyndns.fzoli.mill.server.model;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.DateUtil;
import org.dyndns.fzoli.mill.common.key.ChatKeys;
import org.dyndns.fzoli.mill.common.model.pojo.ChatData;
import org.dyndns.fzoli.mill.common.model.pojo.ChatEvent;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mill.server.model.entity.Message;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class ChatModel extends AbstractOnlineModel<ChatEvent, ChatData> implements ChatKeys {

    private static PlayerDAO DAO = new PlayerDAO();
    
    @Override
    protected ChatData getProperties(HttpServletRequest hsr, RequestMap rm) {
        Player me = getPlayer();
        String action = rm.getFirst(KEY_REQUEST);
        if (me != null && action != null) {
            if (action.equals(REQ_GET_MESSAGES)) {
                String player = rm.getFirst(KEY_PLAYER);
                if (player != null) {
                    Player p = DAO.getPlayer(player);
                    if (p != null) {
                        try { // return every message after the specified date
                            Date d = new Date(Long.parseLong(rm.getFirst(KEY_DATE)));
                            return new ChatData(me.getPlayerName(), ConvertUtil.createMessageList(me.getMessages(p, d)));
                        }
                        catch (Exception ex) { // return only unreaded messages if date is not specified
                            return new ChatData(me.getPlayerName(), ConvertUtil.createMessageList(me.getUnreadedMessages(p)));
                        }
                    }
                }
            }
        }
        return new ChatData(getPlayerName(), me == null ? null : me.getUnreadedMessagesCount());
    }

    @Override
    protected int askModel(HttpServletRequest hsr, RequestMap rm) {
        Player me = getPlayer();
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null && action.equals(REQ_SYNC)) {
            try {
                long remote = Long.parseLong(rm.getFirst(KEY_DATE));
                long local = DateUtil.getDateInTimeZone(new Date(), "GMT").getTime();
                return (int)(local - remote);
            }
            catch (Exception ex) {
                return 0;
            }
        }
        if (me != null && action != null) {
            String player = rm.getFirst(KEY_PLAYER);
            if (player != null) {
                Player p = DAO.getPlayer(player);
                if (p != null) {
                    boolean friends = (p.getFriendList().contains(me) && me.getFriendList().contains(p))/* || me.canUsePermission(p, Permission.CHAT_EVERYONE)*/;
                    if (action.equals(REQ_UPDATE_READ_DATE)) {
                        if (me.updateMessageReadDate(p)) {
                            DAO.save(me);
                            return 1;
                        }
                        else {
                            return 0;
                        }
                    }
                    if (action.equals(REQ_REMOVE_MESSAGES)) {
                        if (DAO.removeMessages(me, p)) {
                            if (friends) callOnPlayerChanged(ChatModel.class, new ChatEvent(p.getPlayerName(), me.getPlayerName()));
                            return 1;
                        }
                        else {
                            return 0;
                        }
                    }
                    if (friends) {
                        String value = rm.getFirst(KEY_VALUE);
                        if (value != null) {
                            if (action.equals(REQ_SEND_MESSAGE)) {
                                Message msg = new Message(p, value);
                                DAO.save(msg);
                                me.getPostedMessages().add(msg);
                                DAO.save(me);
                                msg.setSender(me);
                                callOnPlayerChanged(ChatModel.class, new ChatEvent(p.getPlayerName(), ConvertUtil.createMessage(msg/*, me.getName()*/)));
//                                List<PlayerModel> models = findModels(ModelKeys.PLAYER, false, PlayerModel.class);
//                                for (PlayerModel model : models) {
//                                    Player pl = model.getPlayer();
//                                    if (pl != null && p.equals(pl)) {
//                                        model.reinitPlayer();
//                                        break;
//                                    }
//                                }
//                                reinitPlayer();
                                return 1;
                            }
                        }
                    }
                    else {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    protected void onPlayerChanged(ChatEvent evt) {
        if (evt.isClear() && evt.getPlayerName().equals(getPlayerName())) {
            addEvent(evt);
        }
        if (!evt.isClear() && evt.getMessage().getAddress().equals(getPlayerName())) {
            addEvent(evt);
        }
    }

    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }

}