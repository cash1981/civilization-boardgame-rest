package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.BattleAction;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.GameLogAction;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Unit;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Contains player specific resources
 */
@Path("player/{pbfId}")
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
     * Will choose a tech and update the player collection.
     * <p/>
     * This method will also check if other players have chosen
     *
     * @param player
     * @param pbfId
     * @param techName
     * @return
     */
    @PUT
    @Path("/tech/choose")
    @Timed
    public Response chooseTech(@Auth Player player, @PathParam("pbfId") String pbfId, @NotEmpty @QueryParam("name") String techName) {
        playerAction.chooseTech(pbfId, techName, player.getId());
        return Response.noContent().build();
    }

    /**
     * If logged in playerId is specified we will use that, otherwise we will use logged in player.
     * This because a logged in player, might go into someone elses game
     *
     * @param playerId
     * @return
     */
    @GET
    @Path("/tech/{playerId}")
    public Response getChosenTechs(@PathParam("pbfId") String pbfId, @PathParam("playerId") String playerId) {
        Player pl = playerAction.getPlayerById(playerId);
        if (pl == null) {
            log.error("Didn't find player with id " + playerId);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Set<Tech> playersTechs = playerAction.getPlayersTechs(pbfId, playerId);
        return Response.ok().entity(playersTechs).build();
    }

    /**
     * Will end the turn for a given player, and take the next player from the list and
     * make that player current starting player.
     *
     * @param player
     * @param pbfId
     * @return
     */
    @PUT
    @Path("/endturn")
    @Timed
    //TODO test
    public Response endTurn(@Auth Player player, @PathParam("pbfId") String pbfId) {
        boolean success = playerAction.endTurn(pbfId, player.getUsername());

        if (success) return Response.noContent().build();

        return Response.serverError().build();
    }

    /**
     * This method checks whether it is the players turn
     *
     * @param player
     * @param pbfId
     * @return
     */
    @GET
    @Path("/yourturn")
    @Timed
    public boolean isYourTurn(@Auth Player player, @PathParam("pbfId") String pbfId) {
        return playerAction.isYourTurn(pbfId, player.getId());
    }

    /**
     * Not in use, use the one taking item instead
     * @deprecated 
     * @see #revealItem(Player,String,ItemDTO)
     */
    @Deprecated
    @PUT
    @Path("/revealItem/{gameLogId}")
    @Timed
    public Response revealItem(@Auth Player player, @PathParam("pbfId") String pbfId, @PathParam("gameLogId") String gameLogId) {
        playerAction.revealItem(pbfId, player.getId(), gameLogId);
        return Response.ok().build();
    }

    @PUT
    @Path("/revealItem")
    @Timed
    public Response revealItem(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        playerAction.revealItem(pbfId, player.getId(), item);
        return Response.ok().build();
    }

    //@DELETE doesn't work on angularjs with content-type
    @POST
    @Path("/item")
    @Timed
    public Response discardItem(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        playerAction.discardItem(pbfId, player.getId(), item);
        return Response.ok().build();
    }

    @PUT
    @Path("/trade")
    @Timed
    public Response giveItemToPlayer(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        if (player.getId().equals(item.getOwnerId())) {
            log.error("You cannot trade with your self");
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        item.setPbfId(pbfId);
        boolean result = playerAction.tradeToPlayer(item, player.getId());
        if (result) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @PUT
    @Path("/draw/{sheetName}")
    @Timed
    public Response drawItem(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("sheetName") SheetName sheetName) {
        DrawAction drawAction = new DrawAction(db);
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, player.getId(), sheetName);
        if (gameLogOptional.isPresent())
            return Response.ok().build();

        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    /**
     * Draws units from playerhand for battle purposes
     *
     * @param player
     * @param pbfId
     * @param numberOfunits
     * @return
     */
    @PUT
    @Path("/battle/draw")
    @Timed
    public Response drawUnits(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @QueryParam("numOfUnits") int numberOfunits) {
        BattleAction battleAction = new BattleAction(db);

        List<Unit> units = battleAction.drawUnitsFromHand(pbfId, player.getId(), numberOfunits);
        return Response.ok().entity(units).build();
    }

    /**
     * Will end a battle for one player
     * Will set the isBattle = false
     * <p/>
     * Both players need to call this method
     *
     * @param player
     * @param pbfId
     * @param numberOfunits
     * @return
     */
    @PUT
    @Path("/battle/end")
    @Timed
    public Response endBattle(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @QueryParam("numOfUnits") int numberOfunits) {
        BattleAction battleAction = new BattleAction(db);

        battleAction.endBattle(pbfId, player.getId());
        return Response.ok().build();
    }

    /**
     * Returns a list of all undoes that a player needs to vote for
     *
     * @param player
     * @param pbfId
     * @return
     */
    @GET
    @Path("/undo")
    @Timed
    public Response getAllUndoThatNeedsVoteFromPlayer(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId) {
        UndoAction undoAction = new UndoAction(db);
        undoAction.getPlayersActiveUndoes(pbfId, player.getUsername());
        return Response.ok().build();
    }

}
