package org.dyndns.fzoli.mill.common.model.pojo;

/**
 *
 * @author zoli
 */
public class PlayerAvatarEvent extends BaseOnlinePojo {

    private String changedPlayer;
    
    public PlayerAvatarEvent(String playerName, String changedPlayer) {
        super(playerName);
        this.changedPlayer = changedPlayer;
    }

    public String getChangedPlayer() {
        return changedPlayer;
    }
    
}