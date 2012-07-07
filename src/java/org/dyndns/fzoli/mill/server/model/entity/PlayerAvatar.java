package org.dyndns.fzoli.mill.server.model.entity;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;
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

    public byte[] getAvatarArray() {
        return avatar;
    }

    public RenderedImage getAvatarImage() {
        return byteArrayToImage(getAvatarArray());
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

    public void setAvatar(RenderedImage avatar) {
        this.avatar = imageToByteArray(avatar);
    }
    
    public void setTopLeftPoint(Point topLeftPoint) {
        this.topLeftPoint = topLeftPoint;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
    
    public static byte[] imageToByteArray(RenderedImage img) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", os);
        }
        catch (IOException ex) {
            return null;
        }
        return os.toByteArray();
    }
    
    public static RenderedImage byteArrayToImage(byte[] array) {
        try {
            return ImageIO.read(new ByteArrayInputStream(array));
        }
        catch (IOException ex) {
            return null;
        }
    }
    
}