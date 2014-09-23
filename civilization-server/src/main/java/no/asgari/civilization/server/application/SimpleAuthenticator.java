package no.asgari.civilization.server.application;

import com.google.common.base.Optional;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.Cleanup;
import no.asgari.civilization.server.model.Player;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, Player> {
    private final JacksonDBCollection<Player, String> playerCollection;

    public SimpleAuthenticator(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
    }

    @Override
    public Optional<Player> authenticate(BasicCredentials credentials) {
        @Cleanup DBCursor<Player> username = playerCollection.find(DBQuery.is("username", credentials.getUsername()));
        if(username == null || !username.hasNext()) return Optional.absent();

        Player player = username.next();
        if(player.getPassword().equals(credentials.getPassword())) {
            return Optional.of(player);
        }
        return Optional.absent();
    }
}