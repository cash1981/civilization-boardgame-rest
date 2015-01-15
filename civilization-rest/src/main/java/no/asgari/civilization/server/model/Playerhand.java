package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;
import java.util.Set;

/**
 * Each PBF has a playerhand consisting of the player and its items
 */
@Data
@JsonRootName("players")
@NoArgsConstructor
@EqualsAndHashCode(of={"username", "playerId"})
public class Playerhand {
    @NotBlank
    //Can consider using the playerId instead or removing @NotBlank
    private String username;

    @NotBlank
    private String playerId;

    private String color;

    //Only one starting player each turn
    /** Determines whos turn it is each round **/
    private boolean yourTurn = false;

    private List<Item> items = Lists.newArrayList();
    private Set<Tech> techsChosen = Sets.newTreeSet();

    private List<Item> civs = Lists.newArrayList();
    private List<Item> infantries = Lists.newArrayList();
    private List<Item> cultureCards = Lists.newArrayList();
    private List<Item> greatPersons = Lists.newArrayList();
    private List<Item> aircrafts = Lists.newArrayList();
    private List<Item> artilleries = Lists.newArrayList();
    private List<Item> mounteds = Lists.newArrayList();
    private List<Item> huts = Lists.newArrayList();
    private List<Item> villages = Lists.newArrayList();
    private List<Item> tiles = Lists.newArrayList();

    @JsonIgnore
    public void addItem(Item item) {
        items.add(item);
        switch (item.getSheetName()) {
            case CIV:
                civs.add(item);
                break;
            case CULTURE_1:
            case CULTURE_2:
            case CULTURE_3:
                cultureCards.add(item);
                break;
            case AIRCRAFT:
                aircrafts.add(item);
                break;
            case INFANTRY:
                infantries.add(item);
                break;
            case MOUNTED:
                mounteds.add(item);
                break;
            case GREAT_PERSON:
                greatPersons.add(item);
                break;
            case HUTS:
                huts.add(item);
                break;
            case VILLAGES:
                villages.add(item);
                break;
        }

    }

    @JsonIgnore
    public boolean removeItem(Item item) {
        boolean removed = items.remove(item);
        switch (item.getSheetName()) {
            case CIV:
                return civs.remove(item);
            case CULTURE_1:
            case CULTURE_2:
            case CULTURE_3:
                return cultureCards.remove(item);
            case AIRCRAFT:
                return aircrafts.remove(item);
            case INFANTRY:
                return infantries.remove(item);
            case MOUNTED:
                return mounteds.remove(item);
            case GREAT_PERSON:
                return greatPersons.remove(item);
            case HUTS:
                return huts.remove(item);
            case VILLAGES:
                return villages.remove(item);
        }
        return removed;
    }

    @JsonIgnore
    public String green() {
        return "Green";
    }

    @JsonIgnore
    public String yellow() {
        return "Yellow";
    }

    @JsonIgnore
    public String purple() {
        return "Purple";
    }

    @JsonIgnore
    public String red() {
        return "Red";
    }
}
