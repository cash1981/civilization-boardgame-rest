package no.asgari.civilization.server.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import lombok.Cleanup;
import no.asgari.civilization.server.action.PBFAction;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class MongoDBBaseTest extends AbstractMongoDBTest {
    @Test
    public void printAllPBFGames() throws IOException {
        @Cleanup DBCursor<PBF> cursor = pbfCollection.find();
        while (cursor.hasNext()) {
            PBF pbf = cursor.next();
            assertNotNull(pbf);
            System.out.println(pbf.toString());
        }
    }

    @Test(expected = com.mongodb.MongoException.class)
    public void testThatCreatingSameUserThrowsException() throws JsonProcessingException {
        createPlayer("cash1981", pbfId);
    }

    private static Player createPlayer(String username, String pbfId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(pbfId);

        WriteResult<Player, String> writeResult = playerCollection.insert(player);
        System.out.println("Saved player " + writeResult.toString());
        assertNotNull(writeResult.getSavedId());
        return player;
    }

}
