package no.asgari.civilization.representations;

import com.google.common.collect.Lists;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;
import java.util.List;

/**
 * PBF stands for Play By Forum
 */
public class PBF {
    @Id
    @ObjectId
    private String id;

    private List<Player> players = Lists.newArrayList();
    private List<Item> items = Lists.newArrayList();
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

}
