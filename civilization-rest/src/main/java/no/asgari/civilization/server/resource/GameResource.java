package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Undo;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.JacksonDBCollection;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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

    /**
     * This is the default method for this resource.
     * It will return all active games
     *
     * @return
     */
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllGames() {
        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        List<PbfDTO> games = gameAction.getAllActiveGames(pbfCollection);

        return Response.ok()
                .entity(games)
                .build();
    }

    @POST
    @Timed
    public Response createGame(@Valid CreateNewGameDTO dto, @Auth Player player) {
        Preconditions.checkNotNull(dto);
        Preconditions.checkNotNull(player);

        log.info("Creating game " + dto);
        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        String id = gameAction.createNewGame(dto);
        return Response.status(Response.Status.CREATED)
                .location(uriInfo.getAbsolutePathBuilder().path(id).build())
                .entity(id)
                .build();
    }

    @PUT
    @Timed
    @Path("/{pbfId}")
    public Response joinGame(@NotEmpty @PathParam("pbfId") String pbfId, @Auth Player player) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(player);

        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        gameAction.joinGame(pbfId, player.getUsername());
        return Response.ok()
                .location(uriInfo.getAbsolutePathBuilder().path(pbfId).build())
                .build();
    }

    /**
     * Gets all the available techs
     * @param pbfId - The PBF
     * @param player - The Authenticated player
     * @return - Response ok with a list of techs
     */
    @GET
    @Timed
    @Path("/{pbfId}/techs")
    public List<Tech> getAllTechs(@NotEmpty @PathParam("pbfId") String pbfId, @Auth Player player) {
        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        //TODO This will never change, so really it should be cached
        List<Tech> techs = gameAction.getAllTechs(pbfId);
        return techs;
        /*return Response.ok()
                .location(uriInfo.getAbsolutePath())
                .entity(techs)
                .build();*/
    }

}
