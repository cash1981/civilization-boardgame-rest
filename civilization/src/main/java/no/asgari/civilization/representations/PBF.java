package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;
import java.util.List;

/**
 * PBF stands for Play By Forum
 */
@JsonRootName(value="pbf")
public class PBF {
    private String id;

    private List<Player> players = Lists.newArrayList();
    private List<Item> items = Lists.newArrayList();

    private List<Mounted> mounted = Lists.newArrayList();
    private List<Infantry> infantry = Lists.newArrayList();
    private List<Artillery> artillery = Lists.newArrayList();
    private List<Aircraft> aircraft = Lists.newArrayList();

    @NotBlank
    private String name;
    private Date created = new Date();
    private int numOfPlayers;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Number of players playing in this PBF
     */
    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public void setNumOfPlayers(int numOfPlayers) {
        this.numOfPlayers = numOfPlayers;
    }

    public List<Mounted> getMounted() {
        return mounted;
    }

    public void setMounted(List<Mounted> mounted) {
        this.mounted = mounted;
    }

    public List<Infantry> getInfantry() {
        return infantry;
    }

    public void setInfantry(List<Infantry> infantry) {
        this.infantry = infantry;
    }

    public List<Artillery> getArtillery() {
        return artillery;
    }

    public void setArtillery(List<Artillery> artillery) {
        this.artillery = artillery;
    }

    public List<Aircraft> getAircraft() {
        return aircraft;
    }

    public void setAircraft(List<Aircraft> aircraft) {
        this.aircraft = aircraft;
    }
}
