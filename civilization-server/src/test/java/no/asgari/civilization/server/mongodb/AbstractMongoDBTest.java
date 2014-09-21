package no.asgari.civilization.server.mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import no.asgari.civilization.server.action.PBFAction;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.UndoAction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public abstract class AbstractMongoDBTest {
    protected static JacksonDBCollection<PBF, String> pbfCollection;
    protected static JacksonDBCollection<Player, String> playerCollection;
    protected static JacksonDBCollection<Draw, String> drawCollection;
    protected static JacksonDBCollection<UndoAction, String> undoActionCollection;

    protected static String pbfId;
    protected static String playerId;
    protected static String pbfId_2;

    private static MongoClient mongo;

    @BeforeClass
    public static void setup() throws Exception {
        CivBoardGameRandomizerConfiguration configuration = new CivBoardGameRandomizerConfiguration();

        mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        AbstractMongoDBTest.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        AbstractMongoDBTest.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        AbstractMongoDBTest.drawCollection = JacksonDBCollection.wrap(db.getCollection(Draw.COL_NAME), Draw.class, String.class);
        AbstractMongoDBTest.undoActionCollection = JacksonDBCollection.wrap(db.getCollection(UndoAction.COL_NAME), UndoAction.class, String.class);
        playerCollection.drop();
        ;
        pbfCollection.drop();
        drawCollection.drop();
        undoActionCollection.drop();

        playerCollection.createIndex(new BasicDBObject("username", 1), new BasicDBObject("unique", true));

        createNewPBFGame();
        createAnotherPBF();
        playerId = playerCollection.findOne().getId();
    }

    @AfterClass
    public static void cleanup() {
        if (mongo != null) {
            mongo.close();
        }
    }

    private static void createNewPBFGame() throws IOException {
        PBFAction pbfAction = new PBFAction();
        PBF pbf = pbfAction.createNewGame();
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        pbfId = writeResult.getSavedId();
        pbf.getPlayers().add(createPlayer("cash1981", pbfId));
        pbf.getPlayers().add(createPlayer("Karandras1", pbfId));
        pbf.getPlayers().add(createPlayer("Itchi", pbfId));
        pbf.getPlayers().add(createPlayer("Chul", pbfId));
    }

    private static void createAnotherPBF() throws IOException {
        PBFAction pbfAction = new PBFAction();
        PBF pbf = pbfAction.createNewGame();
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        pbfId_2 = writeResult.getSavedId();
        pbf.getPlayers().add(createPlayer("Morthai", pbfId_2));
        pbf.getPlayers().add(createPlayer("CJWF", pbfId_2));
        pbf.getPlayers().add(createPlayer("DaveLuca", pbfId_2));
        pbf.getPlayers().add(createPlayer("Foobar", pbfId_2));
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
