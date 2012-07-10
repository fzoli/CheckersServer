package org.dyndns.fzoli.mill.server.model.dao;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.dyndns.fzoli.mill.common.model.entity.Country;
import org.dyndns.fzoli.mill.common.model.entity.Region;

/**
 *
 * @author zoli
 */
public class CityDAO extends AbstractJdbcDAO {

    private static final String ID = "ID", COUNTRY = "COUNTRY", REGION_CODE = "REGION_CODE", NAME = "NAME";
    
    public List<Country> getCountries() {
        return getCountries(null, null, true);
    }
    
    public Country getCountryById(String id) {
        return getFirst(getCountries(ID, id, true));
    }
    
    public Country getCountryByName(String name) {
        return getFirst(getCountries(NAME, name, true));
    }
    
    public List<Country> findCountries() {
        return getCountries(null, null, false);
    }
    
    public List<Country> findCountriesById(String id) {
        return getCountries(ID, id, false);
    }
    
    public List<Country> findCountriesByName(String name) {
        return getCountries(NAME, name, false);
    }
    
    public List<Region> getRegions() {
        return getRegions(null, null, true);
    }
    
    public Region getRegionById(String id) {
        return getFirst(getRegions(ID, id, true));
    }
    
    public List<Region> getRegionsByName(String name) {
        return getRegions(NAME, name, true);
    }
    
    public List<Region> getRegionsByCountry(String country) {
        return getRegions(COUNTRY, country, true);
    }
    
    public List<Region> getRegionsByRegionCode(String regionCode) {
        return getRegions(REGION_CODE, regionCode, true);
    }
    
    public List<Region> findRegions() {
        return getRegions(null, null, false);
    }
    
    public Region findRegionById(String id) {
        return getFirst(getRegions(ID, id, false));
    }
    
    public List<Region> findRegionsByName(String name) {
        return getRegions(NAME, name, false);
    }
    
    public List<Region> findRegionsByCountry(String country) {
        return getRegions(COUNTRY, country, false);
    }
    
    public List<Region> findRegionsByRegionCode(String regionCode) {
        return getRegions(REGION_CODE, regionCode, false);
    }
    
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
    
    private List<Region> getRegions(final String column, String value, final boolean equals) {
        return getObjects(column, value, equals, Region.class, "REGION");
    }
    
    private List<Country> getCountries(final String column, String value, final boolean equals) {
        return getObjects(column, value, equals, Country.class, "COUNTRY");
    }
    
    private <T> List<T> getObjects(final String column, String value, final boolean equals, final Class<T> clazz, final String from) {
        final List<T> l = new ArrayList<T>();
        value = StringEscapeUtils.escapeSql(value);
        String sql = "SELECT * FROM " + from;
        if (value != null) {
            if (equals) sql += " WHERE UPPER(" + column + ") = '" + value.toUpperCase() + "'";
            else sql += " WHERE LOCATE('" + value.toUpperCase() + "', UPPER(" + column + ")) = 1";
        }
        try {
            final Statement statement = getConnection().createStatement();
            final ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                l.add(createObject(results, clazz));
            }
            results.close();
            statement.close();
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return l;
    }
    
    private static <T> T createObject(ResultSet results, Class<T> clazz) {
        try {
            if (clazz.equals(Country.class)) return (T) new Country(results.getString(ID), results.getString(NAME));
            if (clazz.equals(Region.class)) return (T) new Region(Long.parseLong(results.getString(ID)), results.getString(COUNTRY), results.getString(REGION_CODE), results.getString(NAME));
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static <T> T getFirst(List<T> l) {
        if (l == null || l.isEmpty()) return null;
        return l.get(0);
    }
    
}