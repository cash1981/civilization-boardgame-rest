package no.asgari.civilization.server.application;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.java8.auth.CachingAuthenticator;
import io.dropwizard.java8.auth.basic.BasicAuthProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.resource.GameResource;
import no.asgari.civilization.server.resource.LoginResource;
import no.asgari.civilization.server.resource.PlayerResource;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.mongojack.JacksonDBCollection;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@Log4j
@SuppressWarnings("unchecked")
public class CivilizationApplication extends Application<CivilizationConfiguration> {

    public static void main(String[] args) throws Exception {
        new CivilizationApplication().run(new String[]{"server", "src/main/resources/config.yml"});
    }

    @Override
    public void initialize(Bootstrap<CivilizationConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(CivilizationConfiguration configuration, Environment environment) throws Exception {
        MongoClient mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
        DB db = mongo.getDB(configuration.mongodb);
        MongoManaged mongoManaged = new MongoManaged(mongo);
        //Database
        environment.lifecycle().manage(mongoManaged);

        JacksonDBCollection<Player, String> playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        createIndexForPlayer(playerCollection);
        createUsernameCache(playerCollection);

        //healtcheck
        environment.healthChecks().register("MongoHealthCheck", new MongoHealthCheck(mongo));

        //Resources
        environment.jersey().register(new GameResource(db));
        environment.jersey().register(new LoginResource(db));
        environment.jersey().register(new PlayerResource(db));

        //Authentication

        environment.jersey().register(new BasicAuthProvider<>(new CachingAuthenticator<>(new MetricRegistry(), new CivAuthenticator(db),
                CacheBuilderSpec.parse("expireAfterWrite=120m")), "civilization"));

        // Enable CORS headers
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        //cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.setInitParameter("allowCredentials", "true");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    }

    private void createUsernameCache(JacksonDBCollection<Player, String> playerCollection) {
        LoadingCache<String, String> usernameCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .maximumSize(100)
                .removalListener(lis -> log.debug("Removing " + lis.toString() + " from the usernameCache"))
                .build(new CacheLoader<String, String>() {
                    public String load(String playerId) {
                        return playerCollection.findOneById(playerId).getUsername();
                    }
                });

        CivSingleton.instance().setPlayerCache(usernameCache);
    }

    private void createIndexForPlayer(JacksonDBCollection<Player, String> playerCollection) {
        playerCollection.createIndex(new BasicDBObject(Player.USERNAME, 1), new BasicDBObject("unique", true));
        playerCollection.createIndex(new BasicDBObject(Player.EMAIL, 1), new BasicDBObject("unique", true));
    }

}
