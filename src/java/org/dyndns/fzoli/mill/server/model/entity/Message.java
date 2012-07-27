package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.dyndns.fzoli.mill.common.model.entity.MessageType;
import org.dyndns.fzoli.mill.common.model.entity.MessageType.SystemMessage;

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
    
    @Enumerated(EnumType.STRING)
    private SystemMessage msg;
    
    @OneToMany(/*fetch=FetchType.LAZY, cascade=CascadeType.ALL, */mappedBy="postedMessages")
    private Player sender;
    
    @ManyToOne
    private Player address;
    
    protected Message() {
    }
    
    public Message(Player address, SystemMessage msg) {
        this(address, null, msg);
        this.type = MessageType.SYSTEM;
    }
    
    public Message(Player address, String text) {
        this(address, text, false);
    }
    
    public Message(Player address, String text, boolean support) {
        this(address, text, null);
        this.type = support ? MessageType.SUPPORT : MessageType.CHAT;
    }
    
    private Message(Player address, String text, SystemMessage msg) {
        this.address = address;
        this.text = text;
        this.msg = msg;
    }
    
    public Long getId() {
        return id;
    }

    public MessageType getType() {
        return type;
    }

    public SystemMessage getSystemMessage() {
        return msg;
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

    public void setSystemMessage(SystemMessage msg) {
        this.msg = msg;
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