package no.asgari.civilization.application;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;
import no.asgari.civilization.resource.GameResource;
import org.mongojack.JacksonDBCollection;

public class CivBoardgameRandomizerApplication extends Application<CivBoardGameRandomizerConfiguration> {

    public static void main(String[] args) throws Exception {
        new CivBoardgameRandomizerApplication().run(new String[]{"server", "src/main/resources/config.yml"});
    }

    @Override
    public void initialize(Bootstrap<CivBoardGameRandomizerConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(CivBoardGameRandomizerConfiguration configuration, Environment environment) throws Exception {
        MongoClient mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        JacksonDBCollection<PBF, String> pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        JacksonDBCollection<Player, String> playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        MongoManaged mongoManaged = new MongoManaged(mongo);
        environment.lifecycle().manage(mongoManaged);

        environment.healthChecks().register("MongoHealthCheck", new MongoHealthCheck(mongo));

        environment.jersey().register(new GameResource(pbfCollection, playerCollection));
    }

}
