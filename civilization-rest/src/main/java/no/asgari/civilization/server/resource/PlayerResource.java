package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Player;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.Optional;
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

    /**
     * Will return a colletion of all pbf ids
     */
    @GET @Timed
    public Response getGamesForPlayer(@Auth Player player) {
        Set<String> games = playerAction.getGames(player);
        //TODO Perhaps nice to create location for all the games
        return Response.ok().entity(games).build();
    }

    /**
     * Will choose a tech and update the player collection.
     *
     * This method will also check if other players have chosen
     * @param player
     * @param pbfId
     * @param item
     * @return
     */
    @PUT
    @Path("{pbfId}/tech/choose")
    @Timed
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
    @Timed
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
    @Timed
    public boolean isYourTurn(@Auth Player player, @PathParam("pbfId") String pbfId) {
        return  playerAction.isYourTurn(pbfId, player.getId());
    }

    @PUT
    @Path("{pbfId}/revealItem")
    @Timed
    public Response revealItem(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        playerAction.revealItem(pbfId, player.getId(), item);
        return  Response.ok().build();
    }

    @PUT
    @Path("{pbfId}/trade")
    @Timed
    public Response giveItemToPlayer(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        if(player.getId().equals(item.getOwnerId())) {
            return Response.status(Response.Status.FORBIDDEN).entity("You cannot trade with your self").build();
        }
        item.setPbfId(pbfId);
        boolean result = playerAction.tradeToPlayer(item, player.getId());
        if(result) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @PUT
    @Path("{pbfId}/draw/{sheetName}")
    @Timed
    public Response drawItem(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("sheetName") SheetName sheetName) {
        DrawAction drawAction = new DrawAction(db);
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, player.getId(), sheetName);
        if(gameLogOptional.isPresent())
            return Response.ok().build();
        else return Response.status(Response.Status.NOT_MODIFIED).build();
    }

}
