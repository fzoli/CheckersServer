package org.dyndns.fzoli.mill.common.model.pojo;

import java.util.List;
import org.dyndns.fzoli.mill.common.model.entity.Message;
import org.dyndns.fzoli.mill.common.model.entity.Player;

/**
 *
 * @author zoli
 */
public class ChatData extends BaseOnlinePojo {

    private List<Message> messages;

    public ChatData(String playerName) {
        super(playerName);
    }
    
    public ChatData(String playerName, List<Message> messages) {
        super(playerName);
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }
    
}