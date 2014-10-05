package no.asgari.civilization.server.application;

import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.Authenticator;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

public class CivAuthenticator implements Authenticator<BasicCredentials, Player> {
    private final PlayerAction playerAction;

    public CivAuthenticator(JacksonDBCollection<Player, String> playerCollection) {
        playerAction = new PlayerAction(playerCollection, null);
    }

    @Override
    public Optional<Player> authenticate(BasicCredentials credentials) {
        java.util.Optional<Player> username = playerAction.findPlayerByUsername(credentials.getUsername());

        if(!username.isPresent()) {
            return Optional.empty();
        }

        Player player = username.get();
        if(player.getPassword().equals(DigestUtils.sha1Hex(credentials.getPassword()))) {
            return Optional.of(player);
        }
        return Optional.empty();
    }
}