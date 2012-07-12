package org.dyndns.fzoli.mill.server.model.dao;

/**
 *
 * @author zoli
 */
public class CityDAO extends org.dyndns.fzoli.location.dao.CityDAO {

    @Override
    protected String getLocation() {
        return "tcp://localhost/~/cities";
    }
    
}