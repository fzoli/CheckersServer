package org.dyndns.fzoli.mill.server.model.dao;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.dyndns.fzoli.mill.common.model.entity.Country;

/**
 *
 * @author zoli
 */
public class CityDAO extends AbstractJdbcDAO {

    @Override
    protected String getUrl() {
        return "jdbc:h2:zip:" + new File("cities.h2.zip").getAbsolutePath() + "!/cities";
    }

    @Override
    protected String getDriver() {
        return "org.h2.Driver";
    }

    @Override
    protected String getUser() {
        return "sa";
    }

    @Override
    protected String getPassword() {
        return "";
    }
    
    public List<Country> getCountries() {
        return getCountries(null, null);
    }
    
    public List<Country> getCountriesById(String id) {
        return getCountries("ID", id);
    }
    
    public List<Country> getCountriesByName(String name) {
        return getCountries("NAME", name);
    }
    
    private List<Country> getCountries(String column, String value) {
        List<Country> l = new ArrayList<Country>();
        if (!(column.equals("ID") || column.equals("NAME"))) return l;
        value = StringEscapeUtils.escapeSql(value);
        try {
            String sql = "SELECT * FROM COUNTRY";
            if (column != null && value != null) {
                sql += " WHERE UPPER(" + column + ") = '" + value.toUpperCase() + "'";
            }
            Statement statement = getConnection().createStatement();
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                l.add(new Country(results.getString("ID"), results.getString("NAME")));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return l;
    }
    
}