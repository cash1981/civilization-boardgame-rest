package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Each PBF has a playerhand consisting of the player and its items
 */
@Data
@JsonRootName("players")
@NoArgsConstructor
@EqualsAndHashCode(of = {"username", "playerId"})
public class Playerhand {
    @NotBlank
    //Can consider using the playerId instead or removing @NotBlank
    private String username;

    @NotBlank
    private String playerId;

    private String color;

    //Only one starting player each turn
    /**
     * Determines whos turn it is each round *
     */
    private boolean yourTurn = false;

    private List<Item> items = Lists.newArrayList();
    private Set<Tech> techsChosen = Sets.newTreeSet();

    private List<Item> civs = Lists.newArrayList();
    private List<Item> cultureCards = Lists.newArrayList();
    private List<Item> greatPersons = Lists.newArrayList();
    private List<Item> huts = Lists.newArrayList();
    private List<Item> villages = Lists.newArrayList();
    private List<Item> tiles = Lists.newArrayList();
    private List<Item> citystates = Lists.newArrayList();
    private List<Item> wonders = Lists.newArrayList();
    private List<Unit> units = Lists.newArrayList();
    
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
            case INFANTRY:
            case MOUNTED:
            case ARTILLERY:
                units.add((Unit) item);
                Collections.sort(units);
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
            case CITY_STATES:
                citystates.add(item);
                break;
            case WONDERS:
                wonders.add(item);
                break;
            case TILES:
                tiles.add(item);
                break;
            default:
                throw new RuntimeException("You forgot " + item.getSheetName());
        }

    }

    @JsonIgnore
    public boolean removeItem(Item item) {
        items.remove(item);
        switch (item.getSheetName()) {
            case CIV:
                return civs.remove(item);
            case CULTURE_1:
            case CULTURE_2:
            case CULTURE_3:
                return cultureCards.remove(item);
            case AIRCRAFT:
            case INFANTRY:
            case MOUNTED:
            case ARTILLERY:
                return units.remove(item);
            case GREAT_PERSON:
                return greatPersons.remove(item);
            case HUTS:
                return huts.remove(item);
            case VILLAGES:
                return villages.remove(item);
            case CITY_STATES:
                return citystates.remove(item);
            case WONDERS:
                return wonders.remove(item);
            case TILES:
                return tiles.remove(item);

            default:
                throw new RuntimeException("You forgot " + item.getSheetName());
        }
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
