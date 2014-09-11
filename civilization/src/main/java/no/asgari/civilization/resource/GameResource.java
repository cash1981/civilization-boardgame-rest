package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.excel.PBFTest;
import no.asgari.civilization.representations.PBF;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
