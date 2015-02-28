package no.asgari.civilization.server.mongodb;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.CachingAuthenticator;
import io.dropwizard.java8.auth.basic.BasicAuthProvider;
import io.dropwizard.jersey.DropwizardResourceConfig;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PBFTestAction;
import no.asgari.civilization.server.application.CivAuthenticator;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

@Log4j
public abstract class AbstractMongoDBTest extends JerseyTest {
    protected static JacksonDBCollection<PBF, String> pbfCollection;
    protected static JacksonDBCollection<Player, String> playerCollection;
    protected static JacksonDBCollection<GameLog, String> gameLogCollection;

    protected static String pbfId;
    protected static String playerId;
    protected static String pbfId_2;
    protected static String pbfId_3;

    protected static DB db;

    private static MongoClient mongo;

    @BeforeClass
    public static void setup() throws Exception {
        CivilizationConfiguration configuration = new CivilizationConfiguration();

        mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        db = mongo.getDB(CivilizationConfiguration.CIVILIZATION_TEST);

        AbstractMongoDBTest.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        AbstractMongoDBTest.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        AbstractMongoDBTest.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);

        playerCollection.drop();
        pbfCollection.drop();
        gameLogCollection.drop();

        createIndexForPlayer(playerCollection);

        createNewPBFGame();
        createAnotherPBF();
        createEmptyPBF();
        playerId = playerCollection.findOne().getId();

        createUsernameCache(playerCollection);
    }

    private static void createIndexForPlayer(JacksonDBCollection<Player, String> playerCollection) {
        playerCollection.createIndex(new BasicDBObject(Player.USERNAME, 1), new BasicDBObject("unique", true));
        playerCollection.createIndex(new BasicDBObject(Player.EMAIL, 1), new BasicDBObject("unique", true));
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        final CachingAuthenticator<BasicCredentials, Player> authenticator =
                new CachingAuthenticator<>(new MetricRegistry(),
                        new CivAuthenticator(db),
                        CacheBuilderSpec.parse("maximumSize=1"));
        config.getSingletons().add(new BasicAuthProvider<>(authenticator, "civilization"));
        config.getSingletons().add(new LoginResource(db));
        config.getSingletons().add(new PlayerResource(db));
        config.getSingletons().add(new GameResource(db));

        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @AfterClass
    public static void cleanup() {
        if (mongo != null) {
            mongo.close();
        }

        //CivCache.instance().getEventBus().unregister(new LogListener(gameLogCollection, publicLogCollection));
    }

    protected static String getUsernameAndPassEncoded() {
        return "Basic " + B64Code.encode("cash1981" + ":" + "foo", StringUtil.__ISO_8859_1);
    }

    private static void createNewPBFGame() throws IOException {
        PBFTestAction pbfTestAction = new PBFTestAction();
        PBF pbf = pbfTestAction.createNewGame("First civ game");
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
        PBFTestAction pbfTestAction = new PBFTestAction();
        PBF pbf = pbfTestAction.createNewGame("Second civ game");
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        pbfId_2 = writeResult.getSavedId();

        PBF oneById = pbfCollection.findOneById(pbfId_2);
        oneById.getPlayers().add(createPlayerhand(createPlayer("Morthai", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("CJWF", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("DaveLuca", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Foobar", pbfId_2)));
        pbfCollection.updateById(pbfId_2, oneById);
    }

    private static void createEmptyPBF() throws IOException {
        PBFTestAction pbfTestAction = new PBFTestAction();
        PBF pbf = pbfTestAction.createNewGame("Third civ game");
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        pbfId_3 = writeResult.getSavedId();
    }

    private static Playerhand createPlayerhand(Player player) {
        Playerhand playerhand = new Playerhand();
        playerhand.setUsername(player.getUsername());
        playerhand.setPlayerId(player.getId());
        if (player.getUsername().equals("cash1981")) {
            playerhand.setYourTurn(true);
        }
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

    private static void createUsernameCache(final JacksonDBCollection<Player, String> playerCollection) {
        LoadingCache<String, String> usernameCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .maximumSize(100)
                .build(new CacheLoader<String, String>() {
                    public String load(String playerId) {
                        return playerCollection.findOneById(playerId).getUsername();
                    }
                });

        CivSingleton.instance().setPlayerCache(usernameCache);
    }

}
