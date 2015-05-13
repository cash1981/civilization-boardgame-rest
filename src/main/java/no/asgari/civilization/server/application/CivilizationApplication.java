/*
 * Copyright (c) 2015 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package no.asgari.civilization.server.application;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.java8.auth.AuthFactory;
import io.dropwizard.java8.auth.CachingAuthenticator;
import io.dropwizard.java8.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.resource.AuthResource;
import no.asgari.civilization.server.resource.DrawResource;
import no.asgari.civilization.server.resource.GameResource;
import no.asgari.civilization.server.resource.PlayerResource;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.utilities.Binder;
import org.mongojack.JacksonDBCollection;
import static org.eclipse.jetty.servlets.CrossOriginFilter.*;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j
@SuppressWarnings("unchecked")
public class CivilizationApplication extends Application<CivilizationConfiguration> {

    public static void main(String[] args) throws Exception {
        if(args == null || args.length == 0) {
            new CivilizationApplication().run("server", "src/main/resources/config.yml");
        }
        else {
            new CivilizationApplication().run(args);
        }
    }

    @Override
    public void initialize(Bootstrap<CivilizationConfiguration> bootstrap) {
        bootstrap.addBundle(new Java8Bundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(CivilizationConfiguration configuration, Environment environment) throws Exception {
        DB db;
        MongoClient mongo;
        if(!Strings.isNullOrEmpty(configuration.mongodbUser) && !Strings.isNullOrEmpty(configuration.mongodbPassword)) {
            MongoClientURI clientURI = new MongoClientURI("mongodb://" + configuration.mongodbUser + ":" + configuration.mongodbPassword
                    + "@" + configuration.mongohost + ":" + configuration.mongoport + "/" + configuration.mongodb);

            mongo = new MongoClient(clientURI);
            db = mongo.getDB(clientURI.getDatabase());
        } else {
            mongo = new MongoClient(configuration.mongohost, configuration.mongoport);
            db = mongo.getDB(configuration.mongodb);
        }
        MongoManaged mongoManaged = new MongoManaged(mongo);
        environment.lifecycle().manage(mongoManaged);

        JacksonDBCollection<Player, String> playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        JacksonDBCollection<PBF, String> pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        JacksonDBCollection<Chat, String> chatCollection = JacksonDBCollection.wrap(db.getCollection(Chat.COL_NAME), Chat.class, String.class);
        createUniqueIndexForPlayer(playerCollection);
        createUsernameCache(playerCollection);
        createUniqueIndexForPBF(pbfCollection);
        createIndexForChat(chatCollection);
        createItemCache();

        //healtcheck
        environment.healthChecks().register("MongoHealthCheck", new MongoHealthCheck(mongo));

        //Resources
        environment.jersey().register(new GameResource(db));
        environment.jersey().register(new AuthResource(db));
        environment.jersey().register(new PlayerResource(db));
        environment.jersey().register(new DrawResource(db));

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

        // Configure CORS parameters
        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORSFilter", CrossOriginFilter.class);

        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, environment.getApplicationContext().getContextPath() + "api/*");
        filter.setInitParameter(ALLOWED_METHODS_PARAM, "GET,PUT,POST,OPTIONS");
        filter.setInitParameter(ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin, Accept");
        filter.setInitParameter(ALLOW_CREDENTIALS_PARAM, "true");
    }

    private void createItemCache() {
        CivSingleton.instance().setItemsCache(
                CacheBuilder.newBuilder()
                        .expireAfterWrite(3, TimeUnit.HOURS)
                        .maximumSize(4) //1 for each game type
                        .removalListener(lis -> log.debug("Removing " + lis.getKey() + " from the gameCache"))
                        .build(new CacheLoader<GameType, ItemReader>() {
                            public ItemReader load(GameType type) {
                                ItemReader itemReader = new ItemReader();
                                try {
                                    itemReader.readItemsFromExcel(type);
                                } catch (IOException e) {
                                    log.error("Couldn't read from Excel file " + e.getMessage(), e);
                                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                                }
                                return itemReader;
                            }
                        })
        );
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

    private void createUniqueIndexForPlayer(JacksonDBCollection<Player, String> playerCollection) {
        List<DBObject> indexInfos = playerCollection.getIndexInfo();
        if (indexInfos.isEmpty()) {
            playerCollection.createIndex(new BasicDBObject(Player.USERNAME, 1), new BasicDBObject("unique", true));
            playerCollection.createIndex(new BasicDBObject(Player.EMAIL, 1), new BasicDBObject("unique", true));
        }
    }

    private void createUniqueIndexForPBF(JacksonDBCollection<PBF, String> pbfCollection) {
        if (pbfCollection.getIndexInfo().isEmpty()) {
            pbfCollection.createIndex(new BasicDBObject(PBF.NAME, 1), new BasicDBObject("unique", true));
        }
    }

    private void createIndexForChat(JacksonDBCollection<Chat, String> chatCollection) {
        if (chatCollection.getIndexInfo().isEmpty()) {
            chatCollection.createIndex(new BasicDBObject(Chat.PBFID, 1));
        }
    }
}
