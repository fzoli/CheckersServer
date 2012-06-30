package org.dyndns.fzoli.mill.server.model.dao;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.dyndns.fzoli.mill.common.InputValidator;
import org.dyndns.fzoli.mill.common.key.PlayerBuilderReturn;
import org.dyndns.fzoli.mill.common.key.PlayerReturn;
import org.dyndns.fzoli.mill.server.model.entity.Player;

/**
 *
 * @author zoli
 */
public class PlayerDAO extends AbstractDAO {
    
    public static Player getPlayer(String name) {
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
    
    public static PlayerReturn verify(String name, String password, boolean hash) {
        if (name == null || password == null) return PlayerReturn.NULL;
        if (!InputValidator.isUserIdValid(name) || !InputValidator.isPasswordValid(password, hash)) return PlayerReturn.INVALID;
        Player p = getPlayer(name);
        if (p != null) {
            if (!hash) password = InputValidator.md5Hex(password);
            if (password.equals(p.getPassword())) return PlayerReturn.OK;
        }
        return PlayerReturn.NOT_OK;
    }
    
    public static boolean isPlayerNameExists(String name) {
        return isExists(name, "playerName");
    }

    public static boolean isEmailExists(String email) {
        if (email == null || email != null && email.isEmpty()) return false;
        TypedQuery<Boolean> query = getEntityManager().createQuery("SELECT count(p) > 0 FROM Player p WHERE upper(p.email) = upper(:value) AND p.validated = true", Boolean.class);
        query.setParameter("value", email);
        return isExists(query);
    }

    private static boolean isExists(String value, String property) {
        if (value == null || property == null) return false;
        TypedQuery<Boolean> query = getEntityManager().createQuery("SELECT count(p) > 0 FROM Player p WHERE upper(p." + property + ") = upper(:value)", Boolean.class);
        query.setParameter("value", value);
        return isExists(query);
    }
    
    private static boolean isExists(TypedQuery<Boolean> query) {
        try {
            return query.getSingleResult();
        }
        catch (PersistenceException ex) {
            return false;
        }
    }
    
    public static long getPlayerCount() {
        try {
            return getEntityManager().createQuery("SELECT count(p) FROM Player p", Long.class).getSingleResult();
        }
        catch (PersistenceException ex) {
            return 0;
        }
    }
    
    public static PlayerBuilderReturn createPlayer(Player player, boolean hash) {
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
    
    public static boolean save(Player p) {
        EntityTransaction tr = getEntityManager().getTransaction();
        try {
            tr.begin();
            getEntityManager().persist(p);
            tr.commit();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
}