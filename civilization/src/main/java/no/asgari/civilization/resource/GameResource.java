package no.asgari.civilization.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.excel.PBFTest;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

@Path("/games")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class GameResource {

    JacksonDBCollection<PBF, String> pbfCollection;
    JacksonDBCollection<Player, String> playerCollection;

    public GameResource(JacksonDBCollection<PBF, String> pbfCollection, JacksonDBCollection<Player, String> playerCollection) {
        this.pbfCollection = pbfCollection;
        this.playerCollection = playerCollection;
    }

    @GET
    @Timed
    public List<PBF> getAllGames() {
        DBCursor<PBF> dbCursor = pbfCollection.find();

        if (dbCursor.size() == 0) {
            //FIXME REMOVE, ONLY FOR TESTING PURPOSES
            createNewPBFGame();
        }

        List<PBF> pbfs = new ArrayList<>();
        while (dbCursor.hasNext()) {
            PBF pbf = dbCursor.next();
            pbfs.add(pbf);
        }
        return pbfs;

    }

    @SneakyThrows(IOException.class)
    private PBF createNewPBFGame() {
        PBFTest pbfTest = new PBFTest();
        PBF pbf = pbfTest.createGameTest();
        pbf.setNumOfPlayers(4);
        pbf.setName("Game #100 WaW");
        return pbf;
    }

//    private String createJSONFromPBF(PBF pbf) {
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String json = mapper.writeValueAsString(pbf);
//            log.info(json);
//            return json;
//        } catch (JsonProcessingException e) {
//            log.error("Couldn't create JSON object", e);
//        }
//        return "";
//    }

    /*@POST
    @Timed
    public Response createNewGame(PBF PBF) {
        collection.insert(PBF);
        return Response.noContent().build();
    }
    */
}
