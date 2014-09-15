package no.asgari.civilization.representations;

public class PlayerBuilder {

    public static Player createPlayer(String username, int gameId) {
        Player p = new Player();
        p.getGameIds().add(gameId);
        p.setUsername(username);
        return p;
    }
}
