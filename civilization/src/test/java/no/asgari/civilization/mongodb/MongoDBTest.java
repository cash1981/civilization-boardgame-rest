package no.asgari.civilization.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import lombok.Cleanup;
import no.asgari.civilization.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;
import no.asgari.civilization.test.PBFBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class MongoDBTest {
    private static JacksonDBCollection<PBF, String> pbfCollection;
    private static JacksonDBCollection<Player, String> playerCollection;

    @BeforeClass
    public static void setup() throws Exception {
        CivBoardGameRandomizerConfiguration configuration = new CivBoardGameRandomizerConfiguration();

        MongoClient mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        MongoDBTest.pbfCollection = JacksonDBCollection.wrap(db.getCollection("pbf"), PBF.class, String.class);
        MongoDBTest.playerCollection = JacksonDBCollection.wrap(db.getCollection("player"), Player.class, String.class);

        @Cleanup DBCursor<PBF> dbCursor = pbfCollection.find();

        if(dbCursor.size() == 0) {
            createNewPBFGame();
        }
    }

    @Test
    public void testConvertToJSON() throws IOException {
        @Cleanup DBCursor<PBF> cursor = pbfCollection.find();
        while (cursor.hasNext()) {
            PBF pbf = cursor.next();
            assertNotNull(pbf);
            System.out.println(pbf.toString());
        }
    }

    private static void createNewPBFGame() throws IOException {
        PBFBuilder pbfBuilder = new PBFBuilder();
        PBF pbf = pbfBuilder.createNewGame();
       WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        //TODO This is just test data, should be gotten from somewhere, perhaps retrieved from logged in user, or pathparam
        pbf.getPlayers().add(createPlayer("cash1981", writeResult.getSavedId()));
        pbf.getPlayers().add(createPlayer("Itchi", writeResult.getSavedId()));
        pbf.getPlayers().add(createPlayer("Karandras1", writeResult.getSavedId()));
        pbf.getPlayers().add(createPlayer("Chul", writeResult.getSavedId()));
    }

    private static Player createPlayer(String username, String pbfId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(pbfId);

        WriteResult<Player, String> writeResult = playerCollection.insert(player);
        System.out.println("Lagret player " + writeResult.toString());
        assertNotNull(writeResult.getSavedId());
        return player;
    }

}
