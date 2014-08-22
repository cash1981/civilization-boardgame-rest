package no.asgari.civilization.application;

import com.mongodb.DB;
import com.mongodb.Mongo;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.vz.mongodb.jackson.JacksonDBCollection;
import io.dropwizard.server.;

public class CivilizationBoardgameApplication extends Application<CivilizationBoardgameConfiguration> {

    public static void main(String[] args) throws Exception {
        new Reader30Service().run(new String[] { "server", "src/main/resources/config.yml" });
    }

    @Override
    public void initialize(Bootstrap<CivilizationBoardgameConfiguration> bootstrap) {
        bootstrap.setName("reader30");
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(CivilizationBoardgameConfiguration configuration, Environment environment) throws Exception {
        Mongo mongo = new Mongo(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);

        JacksonDBCollection<Blog, String> blogs = JacksonDBCollection.wrap(db.getCollection("blogs"), Blog.class, String.class);
        MongoManaged mongoManaged = new MongoManaged(mongo);
        environment.manage(mongoManaged);

        environment.addHealthCheck(new MongoHealthCheck(mongo));

        environment.addResource(new PlayerResource(blogs));
        environment.addResource(new GameResource(blogs));
    }

}
