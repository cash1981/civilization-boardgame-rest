package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import no.asgari.civilization.excel.PBFTest;
import no.asgari.civilization.representations.PBF;

import javax.validation.Valid;
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
            pbfTest.createGameTest();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*@POST
    @Timed
    public Response createNewGame(PBF PBF) {
        collection.insert(PBF);
        return Response.noContent().build();
    }
    */
}
