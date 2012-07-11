package org.dyndns.fzoli.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zoli
 */
public abstract class AbstractJdbcDAO {
    
    private static final Map<String, Connection> CONNECTIONS = new HashMap<String, Connection>();
    
    protected abstract String getUrl();
    
    protected abstract String getDriver();
    
    protected abstract String getUser();
    
    protected abstract String getPassword();
    
    protected Connection getConnection() {
        Connection conn = CONNECTIONS.get(getUrl());
        if (conn == null) {
            conn = createConnection();
            CONNECTIONS.put(getUrl(), conn);
        }
        return conn;
    }
    
    private Connection createConnection() {
        try {
            Class.forName(getDriver()).newInstance();
            return DriverManager.getConnection(getUrl(), getUser(), getPassword());
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}