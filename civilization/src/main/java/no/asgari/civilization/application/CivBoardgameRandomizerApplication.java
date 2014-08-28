package no.asgari.civilization.application;

import com.mongodb.DB;
import com.mongodb.Mongo;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.vz.mongodb.jackson.JacksonDBCollection;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.resource.GameResource;

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
        Mongo mongo = new Mongo(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        JacksonDBCollection<PBF, String> games = JacksonDBCollection.wrap(db.getCollection("games"), PBF.class, String.class);
        MongoManaged mongoManaged = new MongoManaged(mongo);
        environment.lifecycle().manage(mongoManaged);

        environment.healthChecks().register("MongoHealthCheck", new MongoHealthCheck(mongo));

        environment.jersey().register(new GameResource(games));
    }

}
