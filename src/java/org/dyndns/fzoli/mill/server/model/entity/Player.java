package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import java.util.*;
import javax.persistence.*;
import org.dyndns.fzoli.mill.common.Permission;
import org.dyndns.fzoli.mill.common.model.entity.OnlineStatus;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;

/**
 *
 * @author zoli
 */
@Entity
public class Player implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    private int permission = 0, activePermission = 0;
    
    private boolean validated = false, suspended = false, avatarEnabled = true;
    
    @Column(nullable = false)
    private String playerName, password, email;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date signUpDate = new Date(), signInDate;

    @Embedded
    @OneToOne
    private PersonalData personalData = new PersonalData();
    
    @Enumerated(EnumType.STRING)
    private OnlineStatus onlineStatus = OnlineStatus.ONLINE;
    
    @ManyToMany
    private List<Player> friendList = new ArrayList<Player>(), 
                         friendWishList = new ArrayList<Player>(), 
                         blockedUserList = new ArrayList<Player>();
    
    @ManyToOne
    private List<Message> postedMessages = new ArrayList<Message>();
    
    private HashMap<Player, Date> messageReadDates = new HashMap<Player, Date>();
    
    private final static PlayerDAO DAO = new PlayerDAO();
    
//    @ManyToMany(mappedBy = "friendWishList")
//    private List<Player> possibleFriends;
//    
//    @ManyToMany(mappedBy = "blockedUserList")
//    private List<Player> invisibleUsers;
//    
//    @OneToMany(mappedBy = "address")
//    private List<Message> receivedMessages;
    
    protected Player() {
    }

    public Player(String playerName, String password, String email) {
        this.playerName = playerName;
        this.password = password;
        this.email = email;
    }
    
    public Long getId() {        
        return id;
    }

    public int getPermissionMask(boolean active) {
        return active ? activePermission : permission;
    }
    
    public List<Permission> getPermissions(boolean active) {
        return getPermissions(getPermissionMask(active));
    }
    
    private static List<Permission> getPermissions(int mask) {
        return Permission.getPermissions(mask);
    }
    
    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public boolean isValidated() {
        return validated;
    }
    
    public boolean isSuspended() {
        return suspended;
    }

    public String getName() {
        if (getPersonalData() == null) return getPlayerName();
        String n = getPersonalData().getName();
        return n == null ? getPlayerName() : n;
    }
    
    public boolean isFriend(Player p) {
        if (p == null) return false;
        List<Player> l = getFriendList();
        if (l != null) {
            for (Player p2 : l) {
                if (p2.getPlayerName().equals(p.getPlayerName())) return true;
            }
        }
        return false;
    }

    public boolean isAvatarEnabled() {
        return avatarEnabled;
    }
    
    public boolean isRoot() {
        return getPermissionMask(false) == Permission.ROOT;
    }
    
    public boolean hasPermission(boolean active, Permission p) {
        return hasPermission(active ? activePermission : permission, p);
    }
    
    private static boolean hasPermission(int mask, Permission p) {
        return p.hasPermission(mask);
    }
    
    public Date getSignInDate() {
        return signInDate;
    }

    public Date getSignUpDate() {
        return signUpDate;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public List<Message> getMessages(Player p, Date d) {
        List<Message> l = new ArrayList<Message>();
        List<Message> receivedMessages = getReceivedMessages();
        synchronized (postedMessages) {
            synchronized (receivedMessages) {
                if (receivedMessages != null && p != null && d != null) {
                    for (Message m : postedMessages) {
                        if (m.getAddress().equals(p) && (m.getSendDate().after(d) || m.getSendDate().equals(d))) l.add(m);
                    }
                    for (Message m : receivedMessages) {
                        if (m.getSender().equals(p) && (m.getSendDate().after(d) || m.getSendDate().equals(d))) l.add(m);
                    }
                    Collections.sort(l, new Comparator<Message>() {

                        @Override
                        public int compare(Message m1, Message m2) {
                            return m1.getSendDate().compareTo(m2.getSendDate());
                        }

                    });
                }
            }
        }
        return l;
    }
    
    public Map<String, Integer> getUnreadedMessagesCount() {
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        List<Message> receivedMessages = getReceivedMessages();
        synchronized (receivedMessages) {
            for (Message m : receivedMessages) {
                Player p = m.getSender();
                if (!counts.containsKey(p.getPlayerName())) {
                    counts.put(p.getPlayerName(), getUnreadedMessages(p).size());
                }
            }
        }
        return counts;
    }
    
    public List<Message> getUnreadedMessages(Player p) {
        List<Message> l = new ArrayList<Message>();
        Date d = messageReadDates.get(p);
        List<Message> receivedMessages = getReceivedMessages();
        synchronized (receivedMessages) {
            if (receivedMessages != null && p != null) {
                if (d == null) {
                    for (Message m : receivedMessages) {
                        if (m.getSender().equals(p)) l.add(m);
                    }
                }
                else {
                    for (Message m : receivedMessages) {
                        if (m.getSender().equals(p) && m.getSendDate().after(d)) l.add(m);
                    }
                }
            }
        }
        return l;
    }
    
//    public List<Message> getReceivedMessages() {
//        return receivedMessages;
//    }
    
    public List<Message> getReceivedMessages() {
        return DAO.getMessages(this);
    }
    
    public List<Message> getPostedMessages() {
        return postedMessages;
    }

    public List<Player> getFriendList() {
        return filterList(friendList);
    }

    public List<Player> getFriendWishList() {
        return filterList(friendWishList);
    }

    public List<Player> getPossibleFriends() {
        return filterList(DAO.getPossibleFriends(this));
    }
    
//    public List<Player> getPossibleFriends() {
//        return filterList(possibleFriends);
//    }
//    
//    public List<Player> getInvisibleUsers() {
//        return invisibleUsers;
//    }
    
    public List<Player> getBlockedUserList() {
        return filterList(blockedUserList);
    }

    private List<Player> filterList(List<Player> ls) {
        List<Player> l = new ArrayList<Player>();
        if (ls == null) return l;
        for (Player p : ls) {
            if (p.isSuspended()) {
                if (canUsePermission(p, Permission.DETECT_SUSPENDED_PLAYER)) l.add(p);
            }
            else {
                l.add(p);
            }
        }
        Collections.sort(l, new Comparator<Player>() {

            @Override
            public int compare(Player p1, Player p2) {
                return p1.getPlayerName().compareToIgnoreCase(p2.getPlayerName());
            }
            
        });
        return l;
    }
    
    public boolean canUsePermission(Player target, Permission permission) {
        if (target.isRoot()) return false; // ha a célpont root, senki nem jogosult joggyakorlásra (még egy másik root sem)
        if (target.hasPermission(false, Permission.SHIELD_MODE) && !isRoot()) return false; // ha a célpontnak van SHIELD_MODE joga ÉS a kérő nem ROOT, akkor nem használhatja a célponton a kért jogot
        return hasPermission(true, permission); // egyéb esetben ha aktív a kért jog, használhatja a célponton azt
    }
    
    public void setPermissionMask(int permission) {
        if (getPermissionMask(false) == Permission.ROOT || permission == Permission.ROOT) return; // ROOT jog nem vehető el és nem is adható a programon belül
        List<Permission> oldPermissions = getPermissions(false);
        List<Permission> activePermissions = getPermissions(true);
        List<Permission> newPermissions = getPermissions(permission);
        for (Permission p : oldPermissions) {
            if (oldPermissions.contains(p) && !newPermissions.contains(p)) activePermissions.remove(p); // az elvett jogokat el kell venni az aktív jogokból
            if (newPermissions.contains(p) && !activePermissions.contains(p) && !p.getGroup().equals(Permission.Group.STATE_INVERSE)) activePermissions.add(p); // az éppen most kapott jogokat az aktív jogokba tenni, ha nem mód csoportban van a jog
        }
        this.permission = permission; // új jog beállítása
        setActivePermissionMask(Permission.getMask(activePermissions)); // új aktív jog beállítása
    }
    
    public void setActivePermissionMask(int permission) {
        if (hasPermission(false, Permission.SHIELD_MODE)) permission = Permission.SHIELD_MODE.incPermission(permission); // ha van shield_mode joga, aktívvá kell tenni minden esetben
        List<Permission> selected = Permission.getPermissions(permission);
        int mask = 0;
        for (Permission p : selected) {
            if (hasPermission(false, p)) mask = p.incPermission(mask);
        }
        this.activePermission = mask;
    }

    public void setAvatarEnabled(boolean avatarEnabled) {
        this.avatarEnabled = avatarEnabled;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
    public void setOnlineStatus(OnlineStatus playerState) {
        this.onlineStatus = playerState;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean updateMessageReadDate(Player p) {
        if (p == null) return false;
        boolean ok = false;
        List<Message> receivedMessages = getReceivedMessages();
        synchronized (receivedMessages) {
            for (Message m : receivedMessages) {
                if (m.getSender().getPlayerName().equals(p.getPlayerName())) {
                    ok = true;
                    break;
                }
            }
        }
        if (ok) messageReadDates.put(p, new Date());
        return ok;
    }
    
    public void updateSignInDate() {
        signInDate = new Date();
    }
    
    public String getFirstName() {
        return getPersonalData() == null ? getPlayerName() : getPersonalData().getFirstName() == null ? getName() : getPersonalData().getFirstName();
    }
    
    @Override
    public String toString() {
        return playerName + '#' + getId();
    }
    
}