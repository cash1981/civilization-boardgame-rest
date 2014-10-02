package no.asgari.civilization.server.application;

import com.google.common.base.Optional;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, Player> {
    private final JacksonDBCollection<Player, String> playerCollection;
    private final PlayerAction playerAction;

    public SimpleAuthenticator(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
        playerAction = new PlayerAction(playerCollection);
    }

    @Override
    public Optional<Player> authenticate(BasicCredentials credentials) {
        java.util.Optional<Player> username = playerAction.findPlayerByUsername(credentials.getUsername());

        if(!username.isPresent()) {
            return Optional.absent();
        }

        Player player = username.get();
        if(player.getPassword().equals(credentials.getPassword())) {
            return Optional.of(player);
        }
        return Optional.absent();
    }
}