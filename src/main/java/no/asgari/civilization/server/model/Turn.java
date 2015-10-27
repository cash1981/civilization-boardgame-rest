package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class  Turn implements Comparable<Turn> {
    private int turnNumber;
    private String username;
    private String setup;
    private String sot;
    private String trade;
    private String cm;
    private String movement;
    private String research;

    public Turn(PlayerTurn playerTurn) {
        this.turnNumber = playerTurn.getTurnNumber();
        this.username = playerTurn.getUsername();
        this.setup = playerTurn.getSetup();
        this.sot = playerTurn.getSot();
        this.trade = playerTurn.getTrade();
        this.cm = playerTurn.getCm();
        this.movement = playerTurn.getMovement();
        this.research = playerTurn.getResearch();
    }

    public void copy(PlayerTurn playerTurn) {
        this.turnNumber = playerTurn.getTurnNumber();
        this.username = playerTurn.getUsername();
        this.setup = playerTurn.getSetup();
        this.sot = playerTurn.getSot();
        this.trade = playerTurn.getTrade();
        this.cm = playerTurn.getCm();
        this.movement = playerTurn.getMovement();
        this.research = playerTurn.getResearch();
    }

    @Override
    public int compareTo(Turn o) {
        int v = Integer.valueOf(turnNumber).compareTo(o.getTurnNumber());
        if (v != 0) {
            return v;
        }
        return username.compareTo(o.getUsername());
    }
}
