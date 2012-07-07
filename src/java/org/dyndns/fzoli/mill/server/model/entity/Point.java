package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author zoli
 */
@Embeddable
public class Point implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int x, y;
    
    public Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
}