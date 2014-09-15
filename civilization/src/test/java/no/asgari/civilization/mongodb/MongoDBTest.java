package no.asgari.civilization.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import lombok.Cleanup;
import no.asgari.civilization.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;
import no.asgari.civilization.test.PBFBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MongoDBTest {
    private static DBCollection pbfCollection;
    private static DBCollection playerCollection;

    @BeforeClass
    public static void setup() throws Exception {
        CivBoardGameRandomizerConfiguration configuration = new CivBoardGameRandomizerConfiguration();

        MongoClient mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        pbfCollection = db.getCollection("pbfCollection");
        playerCollection = db.getCollection("playerCollection");

        @Cleanup DBCursor dbCursor = pbfCollection.find();

        if(dbCursor.size() == 0) {
            createNewPBFGame(1);
        }

    }

    @Test
    public void testConvertToJSON() throws IOException {
        int gameId = 1;

        DBCursor cursor = pbfCollection.find();
        while (cursor.hasNext()) {
            DBObject dbObject = cursor.next();
            System.out.println(dbObject.toString());
            PBF pbf = readfromJSON(dbObject.toString(), PBF.class);
            assertNotNull(pbf);
            assertEquals(gameId, pbf.getGameId());
        }

    }

    private static void createNewPBFGame(int gameId) {
        PBFBuilder pbfBuilder = new PBFBuilder();
        try {
            PBF pbf = pbfBuilder.createGameTest(gameId);

            //TODO This is just test data, should be gotten from somewhere, perhaps retrieved from logged in user, or pathparam
            pbf.getPlayers().add(createPlayer("cash1981", gameId));
            pbf.getPlayers().add(createPlayer("Itchi", gameId));
            pbf.getPlayers().add(createPlayer("Karandras1", gameId));
            pbf.getPlayers().add(createPlayer("Chul", gameId));


            String json = createJSONFromObject(pbf);
            DBObject dbObject = (DBObject) JSON.parse(json);
            WriteResult insert = pbfCollection.insert(dbObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Player createPlayer(String username, int gameId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(gameId);

        String json = createJSONFromObject(player);
        DBObject dbObject = (DBObject) JSON.parse(json);
        WriteResult insert = playerCollection.insert(dbObject);

        return player;
    }

    private static String createJSONFromObject(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(object);
            return json;
        } catch (JsonProcessingException e) {
            throw e;
        }
    }

    private static <T> T readfromJSON(String json, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw e;
        }
    }

}
