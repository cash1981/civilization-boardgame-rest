package no.asgari.civilization.representations;

public class PlayerBuilder {

    public static Player createPlayer(String username) {
        Player p = new Player();
        p.setId("1");
        p.setUsername(username);
        return p;
    }
}
