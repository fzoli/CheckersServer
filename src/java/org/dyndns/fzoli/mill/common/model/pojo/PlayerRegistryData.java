package org.dyndns.fzoli.mill.common.model.pojo;

import java.util.List;
import org.dyndns.fzoli.mill.common.model.entity.BasePlayer;

/**
 *
 * @author zoli
 */
public class PlayerRegistryData extends BaseOnlinePojo {

    private String names, age, sexName, country, region, city;
    
    private int page, lastPage;
    private long count;
    private List<BasePlayer> players;

    public PlayerRegistryData(String playerName, String names, String age, String sexName, String country, String region, String city) {
        super(playerName);
        this.names = names;
        this.age = age;
        this.sexName = sexName;
        this.country = country;
        this.region = region;
        this.city = city;
    }
    
    public PlayerRegistryData(String playerName, List<BasePlayer> players, long count, int page, int lastPage) {
        super(playerName);
        this.players = players;
        this.count = count;
        this.page = page;
        this.lastPage = lastPage;
    }

    public long getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }

    public int getLastPage() {
        return lastPage;
    }

    public List<BasePlayer> getPlayers() {
        return players;
    }

    public String getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getNames() {
        return names;
    }

    public String getRegion() {
        return region;
    }

    public String getSexName() {
        return sexName;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setSexName(String sexName) {
        this.sexName = sexName;
    }
    
}