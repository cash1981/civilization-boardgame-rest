package no.asgari.civilization.representations;

public class GameBuilder {

    public static Game createGame(String id) {
        Game game = new Game();
        game.setId(id);
        return game;
    }

    public static Player createPlayer(String id, String username) {
        Player p = new Player();
        p.setId(id);
        p.setUsername(username);
        return p;
    }
}
