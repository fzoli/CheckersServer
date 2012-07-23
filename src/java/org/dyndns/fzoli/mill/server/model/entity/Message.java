package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author zoli
 */
@Entity
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDate = new Date();

    private String text;
    
    @OneToMany(/*fetch=FetchType.LAZY, cascade=CascadeType.ALL, */mappedBy="postedMessages")
    private Player sender;
    
    @ManyToOne
    private Player address;
    
    protected Message() {
    }
    
    public Message(Player address, String text) {
        this.address = address;
        this.text = text;
    }
    
    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public Player getSender() {
        return sender;
    }

    public Player getAddress() {
        return address;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return super.toString() + '#' + getId();
    }
    
}