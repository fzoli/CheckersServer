package org.dyndns.fzoli.mill.server.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityTransaction;
import org.dyndns.fzoli.mill.server.model.entity.City;
import org.dyndns.fzoli.mill.server.model.entity.Country;
import org.dyndns.fzoli.mill.server.model.entity.Region;

/**
 *
 * @author zoli
 */
public class City2DAO extends AbstractObjectDAO {

    @Override
    protected String getPath() {
        return "cities.odb";
    }

    public boolean save(Country country) {
        return save(country, Country.class);
    }

    public boolean save(Region region) {
        return save(region, Region.class);
    }

    public boolean save(City city) {
        return save(city, City.class);
    }

    private List<Region> getRegions() {
        return getEntityManager().createQuery("SELECT o FROM Region o", Region.class).getResultList();
    }

    private static String createSql(long from, int count) {
        return "SELECT city.name, city.accent_name, city.latitude, city.longitude, region_code FROM city LEFT JOIN region ON (region.id = city.region) LIMIT " + from + ", " + count;
    }

    private static Region find(List<Region> rs, String code) {
        for (Region r : rs) {
            if (r.getRegionCode().equals(code)) {
                return r;
            }
        }
        return null;
    }

    private static int count = 3000000;
    
    private static void load(long from, Statement s, List<Region> regions, City2DAO dao2) throws SQLException {
        System.out.print(new Date() + ": read from " + from + " .");
        ResultSet result = s.executeQuery(createSql(from, count));
        dao2.getEntityManager().getTransaction().begin();
        System.out.print(".");
        while (result.next()) {
            Region region = find(regions, result.getString("REGION_CODE"));
            City c = new City(region, result.getString("NAME"), result.getString("ACCENT_NAME"), result.getDouble("LATITUDE"), result.getDouble("LONGITUDE"));
            dao2.getEntityManager().persist(c);
        }
        System.out.print(".");
        dao2.getEntityManager().getTransaction().commit();
        result.close();
        System.out.println(" done " + (int)((from / 2533120.0) * 100) + " %");
        load(from + count, s, regions, dao2);
    }

    private static void prepare(CityDAO dao1, City2DAO dao2) {
        List<org.dyndns.fzoli.mill.common.model.entity.Country> countries = dao1.getCountries();
        int i = 0;
        int countriesSize = countries.size();
        for (org.dyndns.fzoli.mill.common.model.entity.Country country : countries) {
            Country c = new Country(country.getID(), country.getName());
            dao2.save(c);
            List<org.dyndns.fzoli.mill.common.model.entity.Region> regions = dao1.getRegionsByCountry(c.getId());
            for (org.dyndns.fzoli.mill.common.model.entity.Region region : regions) {
                Region r = new Region(c, region.getRegionCode(), region.getName());
                dao2.save(r);
            }
            i++;
            System.out.println("country "+i+" / "+countriesSize);
        }
    }
    
    private static void fill(CityDAO dao1, City2DAO dao2) throws SQLException {
        System.out.println("started");
        List<Region> regions = dao2.getRegions();
        Statement s = dao1.getConnection().createStatement();
        System.out.println("connected");
        long start = 0;
        try {
            start = dao2.getEntityManager().createQuery("select count(c) from City c", Long.class).getSingleResult();
        }
        catch (Exception ex) {
            ;
        }
        start += 1011800;
        start += 1010000;
        load(start, s, regions, dao2);
        System.out.println("finished");
    }
    
    public static void main(String[] args) throws SQLException {
        CityDAO dao1 = new CityDAO();
        City2DAO dao2 = new City2DAO();
//        prepare(dao1, dao2);
        fill(dao1, dao2);
    }
    
}