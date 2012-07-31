package org.dyndns.fzoli.mill.common.model.pojo;

import org.dyndns.fzoli.mill.common.model.entity.BasePlayer;

/**
 *
 * @author zoli
 */
public class PlayerRegistryEvent extends BaseOnlinePojo {
    
    public static enum EventType {
        ADD, REMOVE, CHANGE
    }
    
    private EventType type;
    private String reqPlayerName;
    private BasePlayer reqPlayer;
    
    public PlayerRegistryEvent(String playerName, BasePlayer reqPlayer, EventType type) {
        super(playerName);
        init(reqPlayer, type);
    }

    private void init(BasePlayer reqPlayer, EventType type) {
        this.type = type;
        switch (type) {
            case ADD:
            case CHANGE:
                this.reqPlayer = reqPlayer;
                break;
            case REMOVE:
                if (reqPlayer != null) this.reqPlayerName = reqPlayer.getPlayerName();
        }
    }
    
    public EventType getType() {
        return type;
    }
    
    public BasePlayer getRequestPlayer() {
        return reqPlayer;
    }

    public String getRequestPlayerName() {
        if (reqPlayerName == null && reqPlayer != null) return reqPlayer.getPlayerName();
        return reqPlayerName;
    }
    
}