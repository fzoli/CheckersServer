package org.dyndns.fzoli.mill.server.test.objectdb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * Entity class.
 * @author zoli
 */
@Entity
class Player implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String playerName;

    @ManyToMany
    private List<Player> friendWishList = new ArrayList<Player>();
    
    @ManyToMany(mappedBy = "friendWishList")
    private List<Player> possibleFriends;
    
    @ManyToOne
    private List<Message> postedMessages = new ArrayList<Message>();
    
    @OneToMany(mappedBy = "address")
    private List<Message> receivedMessages;
    
    protected Player() {
    }

    public Player(String playerName) {
        this.playerName = playerName;
    }
    
    public Long getId() {        
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }
    
    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }
    
    public List<Message> getPostedMessages() {
        return postedMessages;
    }

    public List<Player> getFriendWishList() {
        return friendWishList;
    }

    public List<Player> getPossibleFriends() {
        return possibleFriends;
    }
    
    @Override
    public String toString() {
        return playerName + '#' + getId();
    }
    
}    

/**
 * Entity class.
 * @author zoli
 */
@Entity
class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDate = new Date();

    private String text;
    
    @OneToMany(mappedBy="postedMessages")
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
    
    @Override
    public String toString() {
        return "Message#" + getId();
    }
    
}