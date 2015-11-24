package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PublicPlayerTurn implements Comparable<PublicPlayerTurn> {
    private int turnNumber = 1;
    private String username = "";
    private Set<String> sotHistory = new HashSet<>();
    private Set<String> tradeHistory = new HashSet<>();
    private Set<String> cmHistory = new HashSet<>();
    private Set<String> movementHistory = new HashSet<>();
    private Set<String> researchHistory = new HashSet<>();

    private String sot = "";
    private String trade = "";
    private String cm = "";
    private String movement = "";
    private String research = "";

    public PublicPlayerTurn(String username, int turnNumber) {
        this.username = username;
        this.turnNumber = turnNumber;
    }

    /**
     * Key is a combination of turn number + username
     */
    @JsonIgnore
    public String getKey() {
        return turnNumber + ":" + username;
    }

    /**
     * If turn locked, then mark as end and increase turnNumber;
     */
    @JsonIgnore
    public boolean endTurn() {
        turnNumber++;
        return true;
    }

    @Override
    public int compareTo(PublicPlayerTurn o) {
        int v = Integer.valueOf(turnNumber).compareTo(o.getTurnNumber());
        if (v != 0) {
            return v;
        }
        return username.compareTo(o.getUsername());
    }

}
