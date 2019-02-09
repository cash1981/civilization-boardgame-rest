package no.asgari.civilization.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.java8.auth.AuthFactory;
import io.dropwizard.java8.auth.CachingAuthenticator;
import io.dropwizard.java8.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import no.asgari.civilization.server.action.PBFTestAction;
import no.asgari.civilization.server.application.CivAuthenticator;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.application.MongoManaged;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.resource.AdminResource;
import no.asgari.civilization.server.resource.AuthResource;
import no.asgari.civilization.server.resource.DrawResource;
import no.asgari.civilization.server.resource.GameResource;
import no.asgari.civilization.server.resource.PlayerResource;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.hk2.utilities.Binder;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@SuppressWarnings("unchecked")
public class CivilizationIntegrationTestApplication extends Application<CivilizationTestConfiguration> {

    public DB db;
    public JacksonDBCollection<PBF, String> pbfRepository;
    public JacksonDBCollection<GameLog, String> gameLogRepository;
    public JacksonDBCollection<Player, String> playerRepository;
    public JacksonDBCollection<Chat, String> chatRepository;
    public String pbfId;
    public String playerId;
    public String pbfId_2;
    public String pbfId_3;

    @Override
    public void initialize(Bootstrap<CivilizationTestConfiguration> bootstrap) {
        bootstrap.addBundle(new Java8Bundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(CivilizationTestConfiguration configuration, Environment environment) throws Exception {
        MongoClient mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        this.db = mongo.getDB(configuration.mongodb);

        MongoManaged mongoManaged = new MongoManaged(mongo);
        //Database
        environment.lifecycle().manage(mongoManaged);

        this.playerRepository = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfRepository = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogRepository = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
        this.chatRepository = JacksonDBCollection.wrap(db.getCollection(Chat.COL_NAME), Chat.class, String.class);

        playerRepository.drop();
        pbfRepository.drop();
        gameLogRepository.drop();
        chatRepository.drop();

        createIndexForPlayer(playerRepository);
        createUsernameCache(playerRepository);
        createIndexForPBF(pbfRepository);
//        createItemCache();

        //Resources
        environment.jersey().register(new GameResource(db));
        environment.jersey().register(new AuthResource(db));
        environment.jersey().register(new PlayerResource(db));
        environment.jersey().register(new DrawResource(db));
        environment.jersey().register(new AdminResource(db));

        //Authenticator
        CachingAuthenticator<BasicCredentials, Player> cachingAuthenticator = new CachingAuthenticator<>(
                new MetricRegistry(),
                new CivAuthenticator(db),
                CacheBuilderSpec.parse("expireAfterWrite=120m")
        );

        //Authentication binder
        Binder authBinder = AuthFactory.binder(new BasicAuthFactory<>(cachingAuthenticator, "civilization", Player.class));

        //Authentication
        environment.jersey().register(authBinder);

        //Test data
        createPlayer("admin", null);

        createNewPBFGame();
        createAnotherPBF();
        createEmptyPBF();

        playerId = playerRepository.find().toArray().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get().getId();
    }

//    private void createItemCache() {
//        CivSingleton.instance().setItemsCache(
//                CacheBuilder.newBuilder()
//                        .maximumSize(4) //1 for each game type
//                        .removalListener(lis -> log.debug("Removing " + lis.getKey() + " from the gameCache"))
//                        .build(new CacheLoader<GameType, ItemReader>() {
//                            public ItemReader load(GameType type) {
//                                ItemReader itemReader = new ItemReader();
//                                try {
//                                    itemReader.readItemsFromExcel(type);
//                                } catch (IOException e) {
//                                    log.error("Couldn't read from Excel file " + e.getMessage(), e);
//                                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//                                }
//                                return itemReader;
//                            }
//                        })
//        );
//    }

    private void createUsernameCache(JacksonDBCollection<Player, String> playerRepository) {
        LoadingCache<String, String> usernameCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .maximumSize(100)
                .removalListener(lis -> log.debug("Removing " + lis.toString() + " from the usernameCache"))
                .build(new CacheLoader<String, String>() {
                    public String load(String playerId) {
                        return playerRepository.findById(playerId).getUsername();
                    }
                });

        CivSingleton.instance().setPlayerCache(usernameCache);
    }

    private void createIndexForPlayer(JacksonDBCollection<Player, String> playerRepository) {
        playerRepository.createIndex(new BasicDBObject(Player.USERNAME, 1), new BasicDBObject("unique", true));
        playerRepository.createIndex(new BasicDBObject(Player.EMAIL, 1), new BasicDBObject("unique", true));
    }

    private void createIndexForPBF(JacksonDBCollection<PBF, String> pbfRepository) {
        pbfRepository.createIndex(new BasicDBObject(PBF.NAME, 1), new BasicDBObject("unique", true));
    }

    private void createNewPBFGame() throws IOException {
        PBFTestAction pbfTestAction = new PBFTestAction();
        PBF pbf = pbfTestAction.createNewGame("First civ game");
        WriteResult<PBF, String> writeResult = pbfRepository.insert(pbf);
        pbfId = writeResult.getSavedId();

        PBF oneById = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand cash1981 = createPlayerhand(createPlayer("cash1981", pbfId));
        cash1981.setGameCreator(true);
        cash1981.setYourTurn(true);
        oneById.getPlayers().add(cash1981);
        oneById.getPlayers().add(createPlayerhand(createPlayer("Karandras1", pbfId)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Itchi", pbfId)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Chul", pbfId)));
        pbfRepository.updateById(pbfId, oneById);
    }

    private void createAnotherPBF() throws IOException {
        PBFTestAction pbfTestAction = new PBFTestAction();
        PBF pbf = pbfTestAction.createNewGame("Second civ game");
        WriteResult<PBF, String> writeResult = pbfRepository.insert(pbf);
        pbfId_2 = writeResult.getSavedId();

        PBF oneById = pbfRepository.findById(pbfId_2);
        Playerhand morthai = createPlayerhand(createPlayer("Morthai", pbfId_2));
        morthai.setGameCreator(true);
        morthai.setYourTurn(true);
        oneById.getPlayers().add(morthai);
        oneById.getPlayers().add(createPlayerhand(createPlayer("CJWF", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("DaveLuca", pbfId_2)));
        oneById.getPlayers().add(createPlayerhand(createPlayer("Foobar", pbfId_2)));
        oneById.setActive(false);
        pbfRepository.updateById(pbfId_2, oneById);
    }

    private void createEmptyPBF() throws IOException {
        PBFTestAction pbfTestAction = new PBFTestAction();
        PBF pbf = pbfTestAction.createNewGame("Third civ game");
        WriteResult<PBF, String> writeResult = pbfRepository.insert(pbf);
        pbfId_3 = writeResult.getSavedId();
    }

    private Playerhand createPlayerhand(Player player) {
        Playerhand playerhand = new Playerhand();
        playerhand.setUsername(player.getUsername());
        playerhand.setPlayerId(player.getId());
        playerhand.setColor("Red");
        return playerhand;
    }

    private Player createPlayer(String username, String pbfId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(pbfId);
        player.setEmail(username + "@mailinator.com");
        player.setPassword(DigestUtils.sha1Hex("foo"));

        WriteResult<Player, String> writeResult = playerRepository.insert(player);
        System.out.println("Saved player " + writeResult.toString());
        player.setId(writeResult.getSavedId());
        return player;
    }

}
