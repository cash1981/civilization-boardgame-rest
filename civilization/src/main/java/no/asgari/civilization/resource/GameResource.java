package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;
import no.asgari.civilization.rest.CreateGameDTO;
import no.asgari.civilization.test.PBFBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/games")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class GameResource {

    private final DBCollection playerCollection;
    private final DBCollection pbfCollection;

    public GameResource(DBCollection pbfCollection, DBCollection playerCollection) {
        this.pbfCollection = pbfCollection;
        this.playerCollection = playerCollection;
    }

    @GET
    @Timed
    public List<DBObject> getAllGames() {
        @Cleanup DBCursor dbCursor = pbfCollection.find();

        if(dbCursor.size() == 0) {
            createNewPBFGame(1);
        }

        List<DBObject> pbfs = new ArrayList<>();
        while (dbCursor.hasNext()) {
            DBObject pbf = dbCursor.next();
            pbfs.add(pbf);
        }

        return pbfs;

    }

    @POST
    @Timed
    @Path("/create")
    public Response createGame(CreateGameDTO createGame) {
        Preconditions.checkNotNull(createGame);

        log.info("Creating game " + createGame);
        //TODO Validate input data

        //TODO Get stuff from Excel

        //TODO Save stuff in mongodb

        //TODO Get id back and return the link to the created game


        return Response.status(200).entity("123").build();


    }

    private void createNewPBFGame(int gameId) {
        PBFBuilder pbfBuilder = new PBFBuilder();
        try {
            PBF pbf = pbfBuilder.createGameTest(gameId);

            //TODO This is just test data, should be gotten from somewhere, perhaps retrieved from logged in user, or pathparam
            pbf.getPlayers().add(createPlayer("cash1981", gameId));
            pbf.getPlayers().add(createPlayer("Itchi", gameId));
            pbf.getPlayers().add(createPlayer("Karandras1", gameId));
            pbf.getPlayers().add(createPlayer("Chul", gameId));


            String json = createJSONFromObject(pbf);
            DBObject dbObject = (DBObject) JSON.parse(json);
            WriteResult insert = pbfCollection.insert(dbObject);
            log.info("Saved PBF to MongoDB " + insert);
        } catch (Exception e) {
            log.error("Couldn't create test game", e);
            throw new RuntimeException(e);
        }
    }

    private Player createPlayer(String username, int gameId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(gameId);

        String json = createJSONFromObject(player);
        DBObject dbObject = (DBObject) JSON.parse(json);
        WriteResult insert = playerCollection.insert(dbObject);
        log.info("Saved Player to MongoDB " + insert);

        return player;
    }

    private String createJSONFromObject(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(object);
            log.info(json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Couldn't create JSON object", e);
            throw e;
        }
    }

    /*@POST
    @Timed
    public Response createNewGame(PBF PBF) {
        pbfCollection.insert(PBF);
        return Response.noContent().build();
    }
    */
}
