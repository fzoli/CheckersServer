package org.dyndns.fzoli.mill.server.model;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
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
        return new ChatData(getPlayerName());
    }

    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        Player me = getPlayer();
        String action = rm.getFirst(KEY_REQUEST);
        if (me != null && action != null) {
            String player = rm.getFirst(KEY_PLAYER);
            if (player != null) {
                Player p = DAO.getPlayer(player);
                if (p != null) {
                    if (action.equals(REQ_UPDATE_READ_DATE)) {
                        me.updateMessageReadDate(p);
                        DAO.save(me);
                        return 1;
                    }
                    String value = rm.getFirst(KEY_VALUE);
                    if (value != null) {
                        if (action.equals(REQ_SEND_MESSAGE)) {
                            Message msg = new Message(p, value);
                            me.getPostedMessages().add(msg);
                            DAO.save(me);
                            callOnPlayerChanged(p, new ChatEvent(me.getPlayerName(), ConvertUtil.createMessage(msg)));
                            return 1;
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override
    protected void onPlayerChanged(Player p, ChatEvent evt) {
        if (evt.getMessage().getAddress().equals(getPlayerName())) {
            addEvent(evt);
        }
    }

}