package no.asgari.civilization.representations;

public class PlayerBuilder {

    public static Player createPlayer(String username) {
        Player p = new Player();
        p.setUsername(username);
        return p;
    }
}
