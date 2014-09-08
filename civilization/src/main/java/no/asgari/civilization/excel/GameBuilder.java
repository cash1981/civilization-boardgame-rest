package no.asgari.civilization.excel;

import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;

public class GameBuilder {

    public static PBF createPBF() {
        PBF pbf = new PBF();
        return pbf;
    }

    public static Player createPlayer(String username) {
        Player p = new Player();
        p.setUsername(username);
        return p;
    }

}
