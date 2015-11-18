package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PlayerTurn implements Comparable<PlayerTurn> {
    private int turnNumber = 1;
    private String username = "";
    private boolean disabled;
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

    public PlayerTurn(String username, int turnNumber) {
        this.username = username;
        this.turnNumber = turnNumber;
    }

    /**
     * Only one instance of username and turnNumber
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerTurn that = (PlayerTurn) o;
        return Objects.equals(turnNumber, that.turnNumber) &&
                Objects.equals(username, that.username);
    }

    /**
     * Only one instance of username and turnNumber
     */
    @Override
    public int hashCode() {
        return Objects.hash(turnNumber, username);
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
    public int compareTo(PlayerTurn o) {
        int v = Integer.valueOf(turnNumber).compareTo(o.getTurnNumber());
        if (v != 0) {
            return v;
        }
        return username.compareTo(o.getUsername());
    }
}
