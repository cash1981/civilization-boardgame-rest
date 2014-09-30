package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Undo;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Path("/game")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Log4j
public class GameResource {
    @Context
    private UriInfo uriInfo;

    private final JacksonDBCollection<Draw, String> drawCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<Undo, String> undoActionCollection;

    public GameResource(JacksonDBCollection<PBF, String> pbfCollection, JacksonDBCollection<Player, String> playerCollection,
                        JacksonDBCollection<Draw, String> drawCollection, JacksonDBCollection<Undo, String> undoActionCollection) {
        this.playerCollection = playerCollection;
        this.pbfCollection = pbfCollection;
        this.drawCollection = drawCollection;
        this.undoActionCollection = undoActionCollection;
    }

    @GET
    @Timed

    public List<PBF> getAllGames() {
        @Cleanup DBCursor<PBF> dbCursor = pbfCollection.find();

        List<PBF> pbfs = new ArrayList<>();
        while (dbCursor.hasNext()) {
            PBF pbf = dbCursor.next();
            pbfs.add(pbf);
        }
        return pbfs;
    }

    @POST
    @Timed
    //TODO Secure it and put @Valid
    public Response createGame(CreateNewGameDTO dto) {
        Preconditions.checkNotNull(dto);

        log.info("Creating game " + dto);
        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        String id = gameAction.createNewGame(dto);
        return Response.status(Response.Status.CREATED)
                .location(uriInfo.getAbsolutePathBuilder().path(id).build())
                .build();
    }

}
