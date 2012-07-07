package org.dyndns.fzoli.mill.common.model.pojo;

/**
 *
 * @author zoli
 */
public class PlayerAvatarData extends BaseOnlinePojo {

    private Integer x, y, scale;
    
    public PlayerAvatarData(String playerName, Integer x, Integer y, Integer scale) {
        super(playerName);
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getScale() {
        return scale;
    }
    
}