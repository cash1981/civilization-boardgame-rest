package no.asgari.civilization.server.resource;

import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.Player;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Set;

/**
 * Contains player specific resources
 */
@Path("player")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class PlayerResource {
    private final DB db;
    private final PlayerAction playerAction;

    @Context
    private UriInfo uriInfo;

    public PlayerResource(DB db) {
        this.db = db;
        playerAction = new PlayerAction(db);
    }

    @GET
    public Response getGamesForPlayer(@Auth Player player) {
        Set<String> games = playerAction.getGames(player);
        //TODO Perhaps nice to create location for all the games
        return Response.ok().entity(games).build();
    }

    /**
     * Will choose a tech and update the player collection
     * @param player
     * @param pbfId
     * @param item
     * @return
     */
    @PUT
    @Path("{pbfId}/tech/choose")
    public Response chooseTech(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        playerAction.chooseTech(pbfId, item, player.getId());
        return Response.noContent().build();
    }

    /**
     * Will end the turn for a given player, and take the next player from the list and
     * make that player current starting player.
     * @param player
     * @param pbfId
     * @return
     */
    @PUT
    @Path("{pbfId}/endturn")
    //TODO test
    public Response endTurn(@Auth Player player, @PathParam("pbfId") String pbfId) {
        boolean success = playerAction.endTurn(pbfId, player.getUsername());

        if(success) return Response.noContent().build();

        return Response.serverError().build();
    }

    /**
     * This method checks whether it is the players turn
     * @param player
     * @param pbfId
     * @return
     */
    @GET
    @Path("{pbfId}/yourturn")
    public boolean isYourTurn(@Auth Player player, @PathParam("pbfId") String pbfId) {
        return  playerAction.isYourTurn(pbfId, player.getId());
    }

    @PUT
    @Path("{pbfId}/revealItem")
    public Response revealItem(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        playerAction.revealItem(pbfId, player.getId(), item);
        return  Response.ok().build();
    }

}
