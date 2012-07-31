package org.dyndns.fzoli.mill.server.model.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.apache.commons.lang.time.DateUtils;
import org.dyndns.fzoli.mill.common.InputValidator;
import org.dyndns.fzoli.mill.common.InputValidator.AgeInterval;
import org.dyndns.fzoli.mill.common.Permission;
import org.dyndns.fzoli.mill.common.key.PlayerBuilderReturn;
import org.dyndns.fzoli.mill.common.key.PlayerReturn;
import org.dyndns.fzoli.mill.common.model.entity.Sex;
import org.dyndns.fzoli.mill.server.model.entity.Message;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import sun.net.ftp.FtpDirEntry;

/**
 *
 * @author zoli
 */
public class PlayerDAO extends AbstractObjectDAO {
    
    private static final boolean DELETE_MESSAGES = true;
    
    public List<Player> getPossibleFriends(Player player) {
        try {
            return getEntityManager().createQuery("SELECT p FROM Player p WHERE :player MEMBER OF p.friendWishList", Player.class).setParameter("player", player).getResultList();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public List<Message> getMessages(Player address) {
        try {
            TypedQuery<Message> query = getEntityManager().createQuery("SELECT m FROM Message m WHERE m.address = :address", Message.class);
            return query.setParameter("address", address).getResultList();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public boolean removeMessages(Player p1, Player p2) {
        if (p1 == null || p2 == null) return false;
        try {
            List<Message> messages = getEntityManager().createQuery("SELECT m FROM Message m WHERE (m.sender.playerName = :p1 AND m.address.playerName = :p2) OR (m.sender.playerName = :p2 AND m.address.playerName = :p1)", Message.class)
                .setParameter("p1", p1.getPlayerName())
                .setParameter("p2", p2.getPlayerName())
                .getResultList();
            if (DELETE_MESSAGES) {
                EntityTransaction tr = getEntityManager().getTransaction();
                tr.begin();
                getEntityManager().createQuery("DELETE FROM Message m WHERE (m.sender.playerName = :p1 AND m.address.playerName = :p2) OR (m.sender.playerName = :p2 AND m.address.playerName = :p1)")
                        .setParameter("p1", p1.getPlayerName())
                        .setParameter("p2", p2.getPlayerName())
                        .executeUpdate();
                tr.commit();
            }
            for (Message m : messages) {
                Player p = m.getSender();
                p.getPostedMessages().remove(m);
                save(p);
            }
            return true;
        }
        catch (PersistenceException ex) {
            return false;
        }
    }
    
    public List<Player> getPlayers() {
        try {
            return getEntityManager().createQuery("SELECT p FROM Player p", Player.class).getResultList();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public long getPlayerCount(Player asker, String names, String age, String sexName, String country, String region, String city) {
        InputValidator.AgeInterval ages = InputValidator.getAges(age);
        return getPlayerCount(asker, names, ages.getFrom(), ages.getTo(), getSex(sexName), country, region, city);
    }
    
    public List<Player> getPlayers(int page, Player asker, String names, String age, String sexName, String country, String region, String city) {
        InputValidator.AgeInterval ages = InputValidator.getAges(age);
        return getPlayers(page, asker, names, ages.getFrom(), ages.getTo(), getSex(sexName), country, region, city);
    }
    
    public long getPlayerCount(Player asker, String names, Integer ageFrom, Integer ageTo, Sex sex, String country, String region, String city) {
        try {
            List<Long> l = getPlayers(Long.class, asker, null, names, ageFrom, ageTo, sex, country, region, city);
            if (l.isEmpty()) return 0;
            return l.get(0);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    
    public List<Player> getPlayers(int page, Player asker, String names, Integer ageFrom, Integer ageTo, Sex sex, String country, String region, String city) {
        return getPlayers(Player.class, asker, page, names, ageFrom, ageTo, sex, country, region, city);
    }
    
    private <T> List<T> getPlayers(Class<T> clazz, Player asker, Integer page, String names, Integer ageFrom, Integer ageTo, Sex sex, String country, String region, String city) {
        if (country == null || country.trim().isEmpty()) country = region = city = null;
        else if (region == null || region.trim().isEmpty()) region = city = null;
             else if (city != null && city.trim().isEmpty()) city = null;
        try {
            return getEntityManager().createQuery("SELECT " + (clazz.equals(Player.class) ? "p" : "count(p)") + " FROM Player p WHERE "
                    + "(:name IS NULL OR :name = '' OR upper(p.playerName) LIKE upper(:name) OR upper(p.personalData.firstName) LIKE upper(:name) OR upper(p.personalData.lastName) LIKE upper(:name)) AND "
                    + "(:dateFrom IS NULL OR :dateTo IS NULL OR p.personalData.birthDate BETWEEN :dateFrom AND :dateTo) AND "
                    + "(:sex IS NULL OR p.personalData.sex = :sex) AND "
                    + "((:country IS NULL OR :country = '' OR p.personalData.country = :country) AND "
                    + "(:region IS NULL OR :region = '' OR p.personalData.region = :region) AND "
                    + "(:city IS NULL OR :city = '' OR p.personalData.city = :city)) AND "
                    + "(p.activePermission NOT MEMBER OF :perms OR (:perm = :root AND NOT p.permission = :root) OR (:shield)) AND "
                    + "(NOT p = :asker)", clazz)
                    .setMaxResults(25)
                    .setFirstResult(clazz.equals(Player.class) && page > 0 ? (page - 1) * 25 : 0)
                    .setParameter("name", names)
                    .setParameter("dateFrom", ageTo == null ? null : DateUtils.addYears(new Date(), -1 * ageTo))
                    .setParameter("dateTo", ageFrom == null ? null : DateUtils.addYears(new Date(), -1 * ageFrom))
                    .setParameter("sex", sex)
                    .setParameter("country", country)
                    .setParameter("region", region)
                    .setParameter("city", city)
                    .setParameter("asker", asker)
                    .setParameter("root", Permission.ROOT)
                    .setParameter("shield", asker == null? false : Permission.hasPermission(asker.getPermissionMask(false), Permission.SHIELD_MODE))
                    .setParameter("perm", asker == null ? 0 : asker.getPermissionMask(false))
                    .setParameter("perms", Permission.getMasks(Permission.HIDDEN_MODE))
                    .getResultList();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public Player getPlayer(String name) {
        if (name == null) return null;
        try {
            TypedQuery<Player> query = getEntityManager().createQuery("SELECT p FROM Player p WHERE upper(p.playerName) = upper(:name)", Player.class);
            return query.setParameter("name", name).getSingleResult();
        }
        catch (PersistenceException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public PlayerReturn verify(String name, String password, boolean hash) {
        if (name == null || password == null) return PlayerReturn.NULL;
        if (!InputValidator.isUserIdValid(name) || !InputValidator.isPasswordValid(password, hash)) return PlayerReturn.INVALID;
        Player p = getPlayer(name);
        if (p != null) {
            if (!hash) password = InputValidator.md5Hex(password);
            if (password.equals(p.getPassword())) return PlayerReturn.OK;
        }
        return PlayerReturn.NOT_OK;
    }
    
    public boolean isPlayerNameExists(String name) {
        return isExists(name, "playerName");
    }

    public boolean isEmailExists(String email) {
        if (email == null || email != null && email.isEmpty()) return false;
        TypedQuery<Boolean> query = getEntityManager().createQuery("SELECT count(p) > 0 FROM Player p WHERE upper(p.email) = upper(:value) AND p.validated = true", Boolean.class);
        query.setParameter("value", email);
        return isExists(query);
    }

    private boolean isExists(String value, String property) {
        if (value == null || property == null) return false;
        TypedQuery<Boolean> query = getEntityManager().createQuery("SELECT count(p) > 0 FROM Player p WHERE upper(p." + property + ") = upper(:value)", Boolean.class);
        query.setParameter("value", value);
        return isExists(query);
    }
    
    private boolean isExists(TypedQuery<Boolean> query) {
        try {
            return query.getSingleResult();
        }
        catch (PersistenceException ex) {
            return false;
        }
    }
    
    public long getPlayerCount() {
        try {
            return getEntityManager().createQuery("SELECT count(p) FROM Player p", Long.class).getSingleResult();
        }
        catch (PersistenceException ex) {
            return 0;
        }
    }
    
    public PlayerBuilderReturn createPlayer(Player player, boolean hash) {
        if (player == null) {
            return PlayerBuilderReturn.NULL;
        }
        if (!InputValidator.isUserIdValid(player.getPlayerName())) {
            return PlayerBuilderReturn.INVALID_USER;
        }
        if (!hash && !InputValidator.isPasswordValid(player.getPassword())) {
            return PlayerBuilderReturn.INVALID_PASSWORD;
        }
        if (hash && !InputValidator.isPasswordHashValid(player.getPassword())) {
            return PlayerBuilderReturn.INVALID_PASSWORD;
        }
        if (player.getPassword().equals(player.getPlayerName())) {
            return PlayerBuilderReturn.PASSWORD_NOT_USER;
        }
        if (!InputValidator.isEmailValid(player.getEmail())) {
            return PlayerBuilderReturn.INVALID_EMAIL;
        }
        if (isEmailExists(player.getEmail())) {
            return PlayerBuilderReturn.EMAIL_EXISTS;
        }
        if (isPlayerNameExists(player.getPlayerName())) {
            return PlayerBuilderReturn.USER_EXISTS;
        }
        if (!hash) {
            player.setPassword(InputValidator.md5Hex(player.getPassword()));
        }
        player.setEmail(player.getEmail().toLowerCase());
        if (!save(player)) return PlayerBuilderReturn.EXCEPTION;
        return PlayerBuilderReturn.OK;
    }
    
    public boolean save(Player player) {
        return save(player, Player.class);
    }
    
    public boolean save(Message message) {
        return save(message, Message.class);
    }
    
    private Sex getSex(String name) {
        try {
            return Sex.valueOf(name);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
}