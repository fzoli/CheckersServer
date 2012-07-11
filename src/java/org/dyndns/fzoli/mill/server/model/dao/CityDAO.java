package org.dyndns.fzoli.mill.server.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dyndns.fzoli.mill.common.model.entity.City;
import org.dyndns.fzoli.mill.common.model.entity.Country;
import org.dyndns.fzoli.mill.common.model.entity.Region;

/**
 *
 * @author zoli
 */
public class CityDAO extends AbstractJdbcDAO {

    private final Log LOG = LogFactory.getLog(CityDAO.class);
    private static final String ID = "ID", COUNTRY = "COUNTRY", REGION = "REGION", REGION_CODE = "REGION_CODE", NAME = "NAME", ACCENT_NAME = "ACCENT_NAME", POPULATION = "POPULATION", LATITUDE = "LATITUDE", LONGITUDE = "LONGITUDE", CITY = "CITY";

    public Country getCountryByName(String name) {
        return getFirst(getCountries(NAME, name, true));
    }
    
    public List<Region> getRegionsByCountryId(String countryId) {
        return getRegions(COUNTRY, countryId, true);
    }
    
    public List<Region> getRegions(String countryName, String regionName) {
        return getRegionsByCountryAndRegionName(countryName, regionName, true);
    }
    
    public List<Region> getRegionsByCountryName(String countryName) {
        Country country = getCountryByName(countryName);
        if (country == null) return new ArrayList<Region>();
        return getRegionsByCountryId(country.getID());
    }
    
    public List<Country> findCountriesByName(String name) { // country auto complette
        return getCountries(NAME, name, false);
    }
    
    public List<Region> findRegions(String countryName, String regionName) { // region auto complette
        return getRegionsByCountryAndRegionName(countryName, regionName, false);
    }
    
    public List<City> findCities(String countryName, String regionName, String cityName) { // city auto complette
        cityName = StringEscapeUtils.escapeSql(cityName).toUpperCase();
        List<Region> regions = getRegions(countryName, regionName);
        String sql = "SELECT * FROM CITY WHERE REGION IN(";
        for (int i = 0; i < regions.size(); i++) {
            sql += regions.get(i).getID();
            if (i != regions.size() - 1) sql += ", ";
        }
        sql += ") AND (LOCATE('" + cityName + "', UPPER(NAME)) = 1 OR LOCATE('" + cityName + "', UPPER(ACCENT_NAME)) = 1);";
        return getObjects(sql, City.class);
    }
    
    @Override
    protected String getUrl() {
        return "jdbc:h2:~/cities";
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
    
    private List<Region> getRegionsByCountryAndRegionName(String countryName, String regionName, boolean equals) {
        Country country = getCountryByName(countryName);
        if (country == null) return new ArrayList<Region>();
        return getObjects(new String[]{COUNTRY, NAME}, new boolean[]{true}, new String[]{country.getID(), regionName}, new boolean[]{true, equals}, Region.class, REGION);
    }
    
    private List<Region> getRegions(final String column, final String value, final boolean equals) {
        return getObjects(column, value, equals, Region.class, REGION);
    }
    
    private List<Country> getCountries(final String column, final String value, final boolean equals) {
        return getObjects(column, value, equals, Country.class, COUNTRY);
    }
    
    private <T> List<T> getObjects(final String column, String value, final boolean equals, final Class<T> clazz, final String from) {
        return getObjects(new String[]{column}, new boolean[]{false}, new String[]{value}, new boolean[]{equals}, clazz, from);
    }
    
    private <T> List<T> getObjects(final String[] columns, final boolean[] ands, String[] values, final boolean[] equals, final Class<T> clazz, final String from) {
        String sql = "SELECT * FROM " + from;
        if (values != null) {
            sql += " WHERE ";
            final int lastIndex = columns.length - 1;
            for (int i = 0; i <= lastIndex; i++) {
                String value = StringEscapeUtils.escapeSql(values[i]);
                sql += equals[i] ? "UPPER(" + columns[i] + ") = '" + value.toUpperCase() + "'" : "LOCATE('" + value.toUpperCase() + "', UPPER(" + columns[i] + ")) = 1";
                if (i != lastIndex) sql += " " + (ands[i] ? "AND" : "OR") + " ";
            }
        }
        sql += ';';
        LOG.info(sql);
        return getObjects(sql, clazz);
    }
    
    private <T> List<T> getObjects(final String sql, final Class<T> clazz) {
        final List<T> l = new ArrayList<T>();
        try {
            final Statement statement = getConnection().createStatement();
            final ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                Object o = null;
                if (clazz.equals(Country.class)) o = new Country(results.getString(ID), results.getString(NAME));
                if (clazz.equals(Region.class)) o = new Region(results.getLong(ID), results.getString(COUNTRY), results.getString(REGION_CODE), results.getString(NAME));
                if (clazz.equals(City.class)) o = new City(results.getLong(ID), results.getLong(REGION), results.getString(NAME), results.getString(ACCENT_NAME), results.getInt(POPULATION), results.getDouble(LATITUDE), results.getDouble(LONGITUDE));
                if (o != null) l.add((T)o);
            }
            results.close();
            statement.close();
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        return l;
    }
    
    private static <T> T getFirst(final List<T> l) {
        if (l == null || l.isEmpty()) return null;
        return l.get(0);
    }
    
}