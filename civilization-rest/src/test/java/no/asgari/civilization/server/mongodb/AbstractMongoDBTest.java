package no.asgari.civilization.server.mongodb;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.Authenticator;
import io.dropwizard.java8.auth.basic.BasicAuthProvider;
import io.dropwizard.jersey.DropwizardResourceConfig;
import no.asgari.civilization.server.action.LogListener;
import no.asgari.civilization.server.action.PBFAction;
import no.asgari.civilization.server.application.CivAuthenticator;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.model.Undo;
import no.asgari.civilization.server.resource.GameResource;
import no.asgari.civilization.server.resource.LoginResource;
import no.asgari.civilization.server.resource.PlayerResource;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public abstract class AbstractMongoDBTest extends JerseyTest {
    protected static JacksonDBCollection<PBF, String> pbfCollection;
    protected static JacksonDBCollection<Player, String> playerCollection;
    protected static JacksonDBCollection<Draw, String> drawCollection;
    protected static JacksonDBCollection<Undo, String> undoCollection;
    protected static JacksonDBCollection<PrivateLog, String> privateLogCollection;
    protected static JacksonDBCollection<PublicLog, String> publicLogCollection;

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
        AbstractMongoDBTest.undoCollection = JacksonDBCollection.wrap(db.getCollection(Undo.COL_NAME), Undo.class, String.class);
        AbstractMongoDBTest.privateLogCollection= JacksonDBCollection.wrap(db.getCollection(PrivateLog.COL_NAME), PrivateLog.class, String.class);
        AbstractMongoDBTest.publicLogCollection = JacksonDBCollection.wrap(db.getCollection(PublicLog.COL_NAME), PublicLog.class, String.class);

        playerCollection.drop();
        pbfCollection.drop();
        drawCollection.drop();
        undoCollection.drop();
        privateLogCollection.drop();
        publicLogCollection.drop();

        playerCollection.createIndex(new BasicDBObject("username", 1), new BasicDBObject("unique", true));

        createNewPBFGame();
        createAnotherPBF();
        playerId = playerCollection.findOne().getId();

        CivSingleton.getInstance().getEventBus().register(new LogListener(privateLogCollection, publicLogCollection));
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        final Authenticator<BasicCredentials, Player> authenticator = new CivAuthenticator(playerCollection);
        config.getSingletons().add(new BasicAuthProvider<>(authenticator, "civilization"));
        config.getSingletons().add(new LoginResource(playerCollection, pbfCollection));
        config.getSingletons().add(new PlayerResource(playerCollection, pbfCollection));
        config.getSingletons().add(new GameResource(pbfCollection, playerCollection, drawCollection, undoCollection));

        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @AfterClass
    public static void cleanup() {
        if (mongo != null) {
            mongo.close();
        }

        //CivCache.getInstance().getEventBus().unregister(new LogListener(privateLogCollection, publicLogCollection));
    }

    protected static String getUsernameAndPassEncoded() {
        return B64Code.encode("cash1981" + ":" + "foo", StringUtil.__ISO_8859_1);
    }

    private static void createNewPBFGame() throws IOException {
        PBFAction pbfAction = new PBFAction();
        PBF pbf = pbfAction.createNewGame();
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        pbfId = writeResult.getSavedId();

        PBF oneById = pbfCollection.findOneById(pbfId);
        oneById.getPlayers().add(createPlayerhand(createPlayer("cash1981", pbfId)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Karandras1", pbfId)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Itchi", pbfId)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Chul", pbfId)));
        pbfCollection.updateById(pbfId, oneById);

    }

    private static void createAnotherPBF() throws IOException {
        PBFAction pbfAction = new PBFAction();
        PBF pbf = pbfAction.createNewGame();
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        pbfId_2 = writeResult.getSavedId();

        PBF oneById = pbfCollection.findOneById(pbfId_2);
        oneById.getPlayers().add(createPlayerhand(createPlayer("Morthai", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("CJWF", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("DaveLuca", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Foobar", pbfId_2)));
        pbfCollection.updateById(pbfId_2, oneById);
    }

    private static Playerhand createPlayerhand(Player player) {
        Playerhand playerhand = new Playerhand();
        playerhand.setUsername(player.getUsername());
        playerhand.setPlayerId(player.getId());
        return playerhand;
    }

    private static Player createPlayer(String username, String pbfId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(pbfId);
        player.setEmail(username + "@mailinator.com");
        player.setPassword(DigestUtils.sha1Hex("foo"));

        WriteResult<Player, String> writeResult = playerCollection.insert(player);
        System.out.println("Saved player " + writeResult.toString());
        assertNotNull(writeResult.getSavedId());
        player.setId(writeResult.getSavedId());
        return player;
    }

}
