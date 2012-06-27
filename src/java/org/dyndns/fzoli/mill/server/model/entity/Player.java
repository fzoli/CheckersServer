package org.dyndns.fzoli.mill.server.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.dyndns.fzoli.mill.common.model.entity.PlayerState;
import org.dyndns.fzoli.mill.common.permission.Permission;
import org.dyndns.fzoli.mill.common.permission.Permissions;

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
    
    private boolean validated = false;
    
    @Column(nullable = false)
    private String playerName, password, email;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date signUpDate = new Date(), signInDate;

    @Embedded
    @OneToOne
    private PersonalData personalData = new PersonalData();
    
    @Enumerated(EnumType.STRING)
    private PlayerState playerState = PlayerState.ONLINE;
    
    @ManyToMany
    private List<Player> friendList = new ArrayList<Player>(), 
                         friendWishList = new ArrayList<Player>(), 
                         blockedUserList = new ArrayList<Player>();
    
    @ManyToMany(mappedBy = "friendWishList")
    private List<Player> possibleFriends;
    
    @ManyToMany(mappedBy = "blockedUserList")
    private List<Player> invisibleUsers;
    
    @ManyToOne
    private List<Message> postedMessages = new ArrayList<Message>();
    
    @OneToMany(mappedBy = "address")
    private List<Message> receivedMessages;

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
        return Permissions.getPermissions(mask);
    }
    
    public PlayerState getPlayerState() {
        return playerState;
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
    
    public boolean isValidated() {
        return validated;
    }
    
    public boolean isRoot() {
        return getPermissionMask(false) == Permissions.ROOT;
    }
    
    public boolean hasPermission(boolean active, Permission p) {
        return hasPermission(active ? activePermission : permission, p);
    }
    
    private static boolean hasPermission(int mask, Permission p) {
        return Permissions.hasPermission(mask, p);
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

    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public List<Message> getPostedMessages() {
        return postedMessages;
    }

    public List<Player> getFriendList() {
        return friendList;
    }

    public List<Player> getFriendWishList() {
        return friendWishList;
    }

    public List<Player> getPossibleFriends() {
        return possibleFriends;
    }

    public List<Player> getBlockedUserList() {
        return blockedUserList;
    }

    public List<Player> getInvisibleUsers() {
        return invisibleUsers;
    }

    public boolean canUsePermission(Player target, Permission permission) {
        if (target.hasPermission(false, Permission.SHIELD_MODE) && !isRoot()) return false; // ha a célpontnak van SHIELD_MODE joga ÉS a kérő nem ROOT, akkor nem használhatja a célponton a kért jogot
        return hasPermission(true, permission); // egyéb esetben ha aktív a kért jog, használhatja a célponton azt
    }
    
    public void setPermissionMask(int permission) {
        if (getPermissionMask(false) == Permissions.ROOT || permission == Permissions.ROOT) return; // ROOT jog nem vehető el és nem is adható a programon belül
        List<Permission> oldPermissions = getPermissions(false);
        List<Permission> activePermissions = getPermissions(true);
        List<Permission> newPermissions = getPermissions(permission);
        for (Permission p : oldPermissions) {
            if (oldPermissions.contains(p) && !newPermissions.contains(p)) activePermissions.remove(p); // az elvett jogokat el kell venni az aktív jogokból
            if (newPermissions.contains(p) && !activePermissions.contains(p)) activePermissions.add(p); // az éppen most kapott jogokat az aktív jogokba tenni
        }
        this.permission = permission; // új jog beállítása
        setActivePermissionMask(Permissions.getMask(activePermissions)); // új aktív jog beállítása
    }
    
    public void setActivePermissionMask(int permission) {
        if (hasPermission(false, Permission.SHIELD_MODE)) permission = Permissions.incPermission(permission, Permission.SHIELD_MODE); // ha van shield_mode joga, aktívvá kell tenni minden esetben
        this.activePermission = permission;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
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
    
    public void updateSignInDate() {
        signInDate = new Date();
    }
    
    @Override
    public String toString() {
        return playerName + '#' + getId();
    }
    
}