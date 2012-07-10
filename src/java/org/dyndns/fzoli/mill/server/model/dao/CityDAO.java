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
        return getCountries(null, null, true);
    }
    
    public Country getCountryById(String id) {
        return getFirst(getCountries("ID", id, true));
    }
    
    public Country getCountryByName(String name) {
        return getFirst(getCountries("NAME", name, true));
    }
    
    public List<Country> findCountries() {
        return getCountries(null, null, false);
    }
    
    public List<Country> findCountriesById(String id) {
        return getCountries("ID", id, false);
    }
    
    public List<Country> findCountriesByName(String name) {
        return getCountries("NAME", name, false);
    }
    
    private List<Country> getCountries(String column, String value, boolean equals) {
        List<Country> l = new ArrayList<Country>();
        if (!(column.equals("ID") || column.equals("NAME"))) return l;
        value = StringEscapeUtils.escapeSql(value);
        try {
            String sql = "SELECT * FROM COUNTRY";
            if (column != null && value != null) {
                if (equals) sql += " WHERE UPPER(" + column + ") = '" + value.toUpperCase() + "'";
                else sql += " WHERE LOCATE('" + value.toUpperCase() + "', UPPER(" + column + ")) = 1";
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
    
    private static <T> T getFirst(List<T> l) {
        if (l == null || l.isEmpty()) return null;
        return l.get(0);
    }
    
}