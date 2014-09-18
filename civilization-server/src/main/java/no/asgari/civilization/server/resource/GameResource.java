package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.rest.CreateGameDTO;
import no.asgari.civilization.server.excel.PBFBuilder;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/games")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class GameResource {

    JacksonDBCollection<PBF, String> pbfCollection;
    JacksonDBCollection<Player, String> playerCollection;

    public GameResource(JacksonDBCollection<PBF, String> pbfCollection, JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
        this.pbfCollection = pbfCollection;
    }

    @GET
    @Timed
    public List<PBF> getAllGames() {
        @Cleanup DBCursor<PBF> dbCursor = pbfCollection.find();

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

        return Response.status(200).entity(new PBF()).build();
    }

    @SneakyThrows(IOException.class)
    private void createNewPBFGame() {
        PBFBuilder pbfBuilder = new PBFBuilder();
        PBF pbf = pbfBuilder.createNewGame();
        WriteResult<PBF, String> writeResult = pbfCollection.insert(pbf);
        log.info("Saved new PBF " + writeResult + " with _id " + writeResult.getSavedId());

        //TODO This is just test data, should be gotten from somewhere, perhaps retrieved from logged in user, or pathparam
        pbf.getPlayers().add(createPlayer("cash1981", writeResult.getSavedId()));
        pbf.getPlayers().add(createPlayer("Itchi", writeResult.getSavedId()));
        pbf.getPlayers().add(createPlayer("Karandras1", writeResult.getSavedId()));
        pbf.getPlayers().add(createPlayer("Chul", writeResult.getSavedId()));
    }

    private Player createPlayer(String username, String gameId) throws JsonProcessingException {
        //The Player object should be cached and retrieved from cache
        Player player = new Player();
        player.setUsername(username);
        player.getGameIds().add(gameId);

        WriteResult<Player, String> writeResult = playerCollection.insert(player);
        log.info("Saved Player to MongoDB " + writeResult);

        return player;
    }

}
