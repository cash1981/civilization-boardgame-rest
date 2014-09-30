package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.application.CivCache;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Undo;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
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

    /**
     * This is the default method for this resource.
     * It will return all active games
     * @return
     */
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllGames() {
        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        List<PbfDTO> games = gameAction.getAllActiveGames(pbfCollection);

        return  Response.ok()
                .entity(games)
                .build();
    }

    @POST
    @Timed
    //TODO @Valid
    public Response createGame(CreateNewGameDTO dto, @CookieParam("username") String username, @CookieParam("token") String token) {
        Preconditions.checkNotNull(dto);

        if(!hasCookies(username, token)) {
            return Response.temporaryRedirect(
                    uriInfo.getBaseUriBuilder().path("/login").build())
                    .build();
        }

        boolean isLoggedIn = CivCache.getInstance().findUser(username, token);
        if(!isLoggedIn) {
            return Response.status(Response.Status.FORBIDDEN)
                    .location(uriInfo.getBaseUriBuilder().path("/login").build())
                    .build();
        }

        log.info("Creating game " + dto);
        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        String id = gameAction.createNewGame(dto);
        return Response.status(Response.Status.CREATED)
                .location(uriInfo.getAbsolutePathBuilder().path(id).build())
                .build();
    }

    @PUT
    @Timed
    @Path("{pbfId}")
    //TODO Implement Auth. For now its done manually with cookies and token
    public Response joinGame(@PathParam("pbfId") String pbfId, @CookieParam("username") String username, @CookieParam("token") String token) {
        if(!hasCookies(username, token)) {
            return Response.temporaryRedirect(
                    uriInfo.getBaseUriBuilder().path("/login").build())
                    .build();
        }

        boolean isLoggedIn = CivCache.getInstance().findUser(username, token);
        if(!isLoggedIn) {
            return Response.status(Response.Status.FORBIDDEN)
                    .location(uriInfo.getBaseUriBuilder().path("/login").build())
                    .build();
        }

        GameAction gameAction = new GameAction(pbfCollection, playerCollection);
        gameAction.joinGame(pbfId, username);
        return Response.ok()
                .location(uriInfo.getAbsolutePath())
                .build();
    }

    private boolean hasCookies(String username, String token) {
        return !(Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(token));
    }

}
