package org.dyndns.fzoli.mill.server.test.objectdb;

import java.io.Serializable;
import java.util.*;
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

/**
 * Helper class.
 * @author zoli
 */
class Util {
    
    static final String PLAYER1 = "player1", PLAYER2 = "player2";
    
    static EntityManager createEntityManager() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.user", "admin");
        properties.put("javax.persistence.jdbc.password", "admin");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("objectdb://localhost:6136/fzoli_bugreport.odb", properties);
        return emf.createEntityManager();
    }
    
    static long getCount(EntityManager db, Class<?> entityClass) {
        try {
            return db.createQuery("SELECT count(e) FROM " + entityClass.getName() + " e", Long.class).getSingleResult();
        }
        catch (PersistenceException ex) {
            return 0;
        }
    }
    
    static void remove(EntityManager db, Class<?> entityClass) {
        EntityTransaction tr = db.getTransaction();
        tr.begin();
        if (getCount(db, entityClass) != 0) db.createQuery("DELETE FROM " + entityClass.getName() + " e").executeUpdate();
        tr.commit();
    }
    
    static void clearDatabase(EntityManager db) {
        remove(db, Player.class);
        remove(db, Message.class);
    }
    
    static Player getPlayer(EntityManager db, String name) {
        TypedQuery<Player> query = db.createQuery("SELECT p FROM Player p WHERE upper(p.playerName) = upper(:name)", Player.class);
        try {
            return query.setParameter("name", name).getSingleResult();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static List<Message> getMessages(EntityManager db, String senderPlayerName) {
        TypedQuery<Message> query = db.createQuery("SELECT m FROM Message m WHERE m.sender.playerName = :name", Message.class);
        try {
            return query.setParameter("name", senderPlayerName).getResultList();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static boolean save(EntityManager db, Object obj) {
        EntityTransaction tr = db.getTransaction();
        try {
            tr.begin();
            db.persist(obj);
            tr.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
}