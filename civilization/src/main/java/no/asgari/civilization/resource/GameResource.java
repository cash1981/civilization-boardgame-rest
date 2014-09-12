package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.excel.PBFTest;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.rest.CreateGameDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/games")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class GameResource {

    private DBCollection collection;

    public GameResource(DBCollection pbf) {
        this.collection = pbf;
    }

    @GET
    @Timed
    public List<DBObject> getAllGames() {
        DBCursor dbCursor = collection.find();

        if(dbCursor.size() == 0) {
            createTestGame();
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

    private void createTestGame() {
        PBFTest pbfTest = new PBFTest();
        try {
            PBF pbf = pbfTest.createGameTest();
            createJSONFromPBF(pbf);
        } catch (Exception e) {
            log.error("Couldn't create test game", e);
            throw new RuntimeException(e);
        }
    }

    private String createJSONFromPBF(PBF pbf) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(pbf);
            log.info(json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Couldn't create JSON object", e);
        }
        return "";
    }

    /*@POST
    @Timed
    public Response createNewGame(PBF PBF) {
        collection.insert(PBF);
        return Response.noContent().build();
    }
    */
}
