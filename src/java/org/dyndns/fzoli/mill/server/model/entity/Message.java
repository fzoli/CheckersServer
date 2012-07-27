package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.dyndns.fzoli.mill.common.model.entity.MessageType;

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
    
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.CHAT;
    
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

    public MessageType getType() {
        return type;
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

    public void setType(MessageType type) {
        this.type = type;
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