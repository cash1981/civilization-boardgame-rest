package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    private List<Item> items = new ArrayList<>();
    private Set<Tech> techsChosen = new TreeSet<>();
    private List<Unit> barbarians = new ArrayList<>(3);
    
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
