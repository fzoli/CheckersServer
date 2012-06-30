package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 *
 * @author zoli
 */
@Entity
public class Validator implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToOne
    private Player player;
    
    private String validatorKey;

    public Validator() {
    }

    public Validator(Player player, String validatorKey) {
        this.player = player;
        this.validatorKey = validatorKey;
    }

    public Player getPlayer() {
        return player;
    }

    public String getValidatorKey() {
        return validatorKey;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setValidatorKey(String validatorKey) {
        this.validatorKey = validatorKey;
    }
    
}