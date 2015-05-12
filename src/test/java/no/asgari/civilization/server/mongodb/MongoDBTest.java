package no.asgari.civilization.server.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Cleanup;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.WriteResult;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class MongoDBTest extends AbstractCivilizationTest {

    private static Player createPlayer(String username, String pbfId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.setPassword(DigestUtils.sha1Hex("foo"));
        player.getGameIds().add(pbfId);

        WriteResult<Player, String> writeResult = getApp().playerCollection.insert(player);
        System.out.println("Saved player " + writeResult.toString());
        assertNotNull(writeResult.getSavedId());
        return player;
    }

    @Test
    public void printAllPBFGames() throws IOException {
        @Cleanup DBCursor<PBF> cursor = getApp().pbfCollection.find();
        while (cursor.hasNext()) {
            PBF pbf = cursor.next();
            assertNotNull(pbf);
            System.out.println(pbf.toString());
        }
    }

    @Test(expected = com.mongodb.MongoException.class)
    public void testThatCreatingSameUserThrowsException() throws JsonProcessingException {
        createPlayer("cash1981", getApp().pbfId);
    }

}
