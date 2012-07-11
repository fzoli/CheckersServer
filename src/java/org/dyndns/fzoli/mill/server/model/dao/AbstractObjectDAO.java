package org.dyndns.fzoli.mill.server.model.dao;

/**
 *
 * @author zoli
 */
public class AbstractObjectDAO extends org.dyndns.fzoli.dao.AbstractObjectDAO {

    @Override
    protected String getPath() {
        return "mill.odb";
    }
    
}