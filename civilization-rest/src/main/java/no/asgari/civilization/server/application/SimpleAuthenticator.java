package no.asgari.civilization.server.application;

import java.util.Optional;

import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.Authenticator;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, Player> {
    private final PlayerAction playerAction;

    public SimpleAuthenticator(JacksonDBCollection<Player, String> playerCollection) {
        playerAction = new PlayerAction(playerCollection);
    }

    @Override
    public Optional<Player> authenticate(BasicCredentials credentials) {
        java.util.Optional<Player> username = playerAction.findPlayerByUsername(credentials.getUsername());

        if(!username.isPresent()) {
            return Optional.empty();
        }

        Player player = username.get();
        if(player.getPassword().equals(credentials.getPassword())) {
            return Optional.of(player);
        }
        return Optional.empty();
    }
}