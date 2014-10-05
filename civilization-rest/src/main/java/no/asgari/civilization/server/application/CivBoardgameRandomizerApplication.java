package no.asgari.civilization.server.application;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.java8.auth.basic.BasicAuthProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.LogListener;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.model.Undo;
import no.asgari.civilization.server.resource.GameResource;
import no.asgari.civilization.server.resource.LoginResource;
import no.asgari.civilization.server.resource.PlayerResource;
import org.mongojack.JacksonDBCollection;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j
@SuppressWarnings("unchecked")
public class CivBoardgameRandomizerApplication extends Application<CivBoardGameRandomizerConfiguration> {

    public static void main(String[] args) throws Exception {
        new CivBoardgameRandomizerApplication().run(new String[]{"server", "src/main/resources/config.yml"});
    }

    @Override
    public void initialize(Bootstrap<CivBoardGameRandomizerConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(CivBoardGameRandomizerConfiguration configuration, Environment environment) throws Exception {
        MongoClient mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        JacksonDBCollection<PBF, String> pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        JacksonDBCollection<Player, String> playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        JacksonDBCollection<Draw, String> drawCollection = JacksonDBCollection.wrap(db.getCollection(Draw.COL_NAME), Draw.class, String.class);
        JacksonDBCollection<Undo, String> undoActionCollection = JacksonDBCollection.wrap(db.getCollection(Undo.COL_NAME), Undo.class, String.class);
        JacksonDBCollection<PrivateLog, String> privateLogCollection= JacksonDBCollection.wrap(db.getCollection(PrivateLog.COL_NAME), PrivateLog.class, String.class);
        JacksonDBCollection<PublicLog, String> publicLogCollection = JacksonDBCollection.wrap(db.getCollection(PublicLog.COL_NAME), PublicLog.class, String.class);

        createIndexForPlayer(playerCollection);
        MongoManaged mongoManaged = new MongoManaged(mongo);
        //Database
        environment.lifecycle().manage(mongoManaged);

        //healtcheck
        environment.healthChecks().register("MongoHealthCheck", new MongoHealthCheck(mongo));

        //Resources
        environment.jersey().register(new GameResource(pbfCollection, playerCollection, drawCollection, undoActionCollection));
        environment.jersey().register(new LoginResource(playerCollection, pbfCollection));
        environment.jersey().register(new PlayerResource(playerCollection, pbfCollection));

        //Authentication
        environment.jersey().register(new BasicAuthProvider<>(new CivAuthenticator(playerCollection), "civilization"));

        CivCache.getInstance().getEventBus().register(new LogListener(privateLogCollection, publicLogCollection));

        CivCache.getInstance(CacheBuilder.<String, UUID>newBuilder()
                .initialCapacity(100)
                .expireAfterWrite(5, TimeUnit.HOURS)
                .removalListener((RemovalListener) listener -> log.debug("Removing username " + listener.getKey() + " from cache"))
                .build());

    }

    private void createIndexForPlayer(JacksonDBCollection<Player, String> playerCollection) {
        playerCollection.createIndex(new BasicDBObject(Player.USERNAME, 1), new BasicDBObject("unique", true));
    }

}
