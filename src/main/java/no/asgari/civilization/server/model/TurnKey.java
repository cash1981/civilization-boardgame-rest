package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TurnKey implements Comparable<TurnKey> {
    private final int turnNumber;
    private final String username;

    public TurnKey(int turnNumber, String username) {
        this.turnNumber = turnNumber;
        this.username = username;
    }

    @Override
    public int compareTo(TurnKey o) {
        int v = Integer.valueOf(turnNumber).compareTo(o.getTurnNumber());
        if (v != 0) {
            return v;
        }
        return username.compareTo(o.getUsername());
    }

    @Override
    public String toString() {
        return "{" +
                "turnNumber:" + turnNumber +
                ", username:'" + username + "'" +
                "}";
    }
}
