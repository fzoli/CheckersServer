package org.dyndns.fzoli.mill.server.model;

import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.ChatKeys;
import org.dyndns.fzoli.mill.common.model.pojo.ChatData;
import org.dyndns.fzoli.mill.common.model.pojo.ChatEvent;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;

/**
 *
 * @author zoli
 */
public class ChatModel extends AbstractOnlineModel<ChatEvent, ChatData> implements ChatKeys {

    @Override
    protected ChatData getProperties(HttpServletRequest hsr, RequestMap rm) {
        return new ChatData(getPlayerName());
    }

    @Override
    protected int setProperty(HttpServletRequest hsr, RequestMap rm) {
        return 0;
    }
    
}