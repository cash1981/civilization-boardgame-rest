package no.asgari.civilization.server.application;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.Authenticator;
import lombok.Cleanup;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

public class CivAuthenticator implements Authenticator<BasicCredentials, Player> {
    private final JacksonDBCollection<Player, String> playerCollection;

    public CivAuthenticator(DB db) {
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
    }

    @Override
    public Optional<Player> authenticate(BasicCredentials credentials) {
        @Cleanup DBCursor<Player> dbPlayer = playerCollection.find(
                DBQuery.is("username", credentials.getUsername()), new BasicDBObject());

        if(dbPlayer == null || !dbPlayer.hasNext()) {
            return Optional.empty();
        }

        Player player = dbPlayer.next();

        CivSingleton.instance().playerCache().put(player.getId(), player.getUsername());

        if(player.getPassword().equals(DigestUtils.sha1Hex(credentials.getPassword()))) {
            return Optional.of(player);
        }
        return Optional.empty();
    }
}