package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 * @author zoli
 */
@Entity
public class PlayerAvatar implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String playerName;
    private byte[] avatar;
    
    private int scale;
    
    @Embedded
    private Point topLeftPoint;

    public PlayerAvatar() {
    }

    public PlayerAvatar(String playerName, byte[] avatar, Point topLeftPoint, int scale) {
        this.playerName = playerName;
        this.avatar = avatar;
        this.scale = scale;
        this.topLeftPoint = topLeftPoint;
    }

    public Long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public Point getTopLeftPoint() {
        return topLeftPoint;
    }

    public int getScale() {
        return scale;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public void setTopLeftPoint(Point topLeftPoint) {
        this.topLeftPoint = topLeftPoint;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
    
}