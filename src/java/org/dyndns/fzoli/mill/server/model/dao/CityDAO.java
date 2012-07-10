package org.dyndns.fzoli.mill.server.model.dao;

import java.io.File;
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
    private static final String ID = "ID", COUNTRY = "COUNTRY", REGION = "REGION", REGION_CODE = "REGION_CODE", NAME = "NAME", ACCENT_NAME = "ACCENT_NAME", POPULATION = "POPULATION", LATITUDE = "LATITUDE", LONGITUDE = "LONGITUDE", WHERE = " WHERE ";
    
    public List<Country> getCountries() {
        return getCountries(null, null, true);
    }
    
    public Country getCountryById(long id) {
        return getCountryById(Long.toString(id));
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
    
    public Region getRegionById(long id) {
        return getRegionById(Long.toString(id));
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
    
    public List<Region> findRegionsById(String id) {
        return getRegions(ID, id, false);
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
    
    public City getCityById(long id) {
        return getCityById(Long.toString(id));
    }
    
    public City getCityById(String id) {
        return getFirst(getCities(ID, id, true));
    }
    
    public List<City> getCitiesByRegionCode(long regionCode) {
        return getCitiesByRegionCode(Long.toString(regionCode));
    }
    
    public List<City> getCitiesByRegionCode(String regionCode) {
        return getCities(REGION, regionCode, true);
    }
    
    public List<City> getCitiesByName(String name) {
        return getCities(NAME, ACCENT_NAME, name, true);
    }
    
    public List<City> findCitiesById(String id) {
        return getCities(ID, id, false);
    }
    
    public List<City> findCitiesByRegionCode(String regionCode) {
        return getCities(REGION, regionCode, false);
    }
    
    public List<City> findCitiesByName(String name) {
        return getCities(NAME, ACCENT_NAME, name, false);
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
    
    private List<City> getCities(final String column, final String value, final boolean equals) {
        return getCities(column, null, value, equals);
    }
    
    private List<City> getCities(final String column, final String column2, final String value, final boolean equals) {
        return getObjects(column, column2, value, equals, City.class, "CITY");
    }
    
    private List<Region> getRegions(final String column, final String value, final boolean equals) {
        return getObjects(column, null, value, equals, Region.class, "REGION");
    }
    
    private List<Country> getCountries(final String column, final String value, final boolean equals) {
        return getObjects(column, null, value, equals, Country.class, "COUNTRY");
    }
    
    private <T> List<T> getObjects(final String column, final String column2, String value, final boolean equals, final Class<T> clazz, final String from) {
        final List<T> l = new ArrayList<T>();
        String sql = "SELECT * FROM " + from;
        if (value != null) {
            value = StringEscapeUtils.escapeSql(value);
            if (column != null && column2 == null) {
                sql += WHERE + createFilterString(column, value, equals);
            }
            else if (column == null && column2 != null) {
                sql += WHERE + createFilterString(column2, value, equals);
            }
            else if (column != null && column2 != null) {
                if (column.equalsIgnoreCase(column2)) sql += WHERE + createFilterString(column, value, equals);
                else sql += WHERE + createFilterString(column, value, equals) + " OR " + createFilterString(column2, value, equals);
            }
        }
        sql += ';';
        LOG.info(sql);
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
    
    private static String createFilterString(final String column, final String value, final boolean equals) {
        return equals ? "UPPER(" + column + ") = '" + value.toUpperCase() + "'" : "LOCATE('" + value.toUpperCase() + "', UPPER(" + column + ")) = 1";
    }
    
    private static <T> T createObject(final ResultSet results, final Class<T> clazz) {
        try {
            if (clazz.equals(Country.class)) return (T) new Country(results.getString(ID), results.getString(NAME));
            if (clazz.equals(Region.class)) return (T) new Region(results.getLong(ID), results.getString(COUNTRY), results.getString(REGION_CODE), results.getString(NAME));
            if (clazz.equals(City.class)) return (T) new City(results.getLong(ID), results.getLong(REGION), results.getString(NAME), results.getString(ACCENT_NAME), results.getInt(POPULATION), results.getDouble(LATITUDE), results.getDouble(LONGITUDE));
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static <T> T getFirst(final List<T> l) {
        if (l == null || l.isEmpty()) return null;
        return l.get(0);
    }
    
    public static void main(String[] args) {
        CityDAO dao = new CityDAO();
        System.out.println(dao.getCountries());
        System.out.println(dao.findRegionsById("1"));
        System.out.println(dao.findCitiesByName("Budapest"));
    }
    
}