package no.asgari.civilization.representations;

public class GameBuilder {

    public static PBF createGame(String id) {
        PBF PBF = new PBF();
        PBF.setId(id);
        return PBF;
    }

    public static Player createPlayer(String id, String username) {
        Player p = new Player();
        p.setId(id);
        p.setUsername(username);
        return p;
    }
}
