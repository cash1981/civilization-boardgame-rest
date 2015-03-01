package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
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
     * Will remove the tech from the playerhand. Will not update the gamelog since the tech is only for private view
     *
     * @param player
     * @param pbfId
     * @param techName
     * @return - 200 OK
     */
    @DELETE
    @Path("/tech/remove")
    @Timed
    public Response removeTech(@Auth Player player, @PathParam("pbfId") String pbfId, @NotEmpty @QueryParam("name") String techName) {
        boolean removedTech = !Strings.isNullOrEmpty(techName) && playerAction.removeTech(pbfId, techName, player.getId());
        if (removedTech)
            return Response.ok().build();

        return Response.status(Response.Status.BAD_REQUEST).build();
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

    @PUT
    @Path("/item/reveal")
    @Timed
    public Response revealItem(@Auth Player player, @PathParam("pbfId") String pbfId, @Valid ItemDTO item) {
        playerAction.revealItem(pbfId, player.getId(), item);
        return Response.ok().build();
    }

    /**
     * Reveals a tech
     * <p/>
     * Will throw BAD_REQUEST if undo has already been performed
     *
     * @param player
     * @param pbfId
     * @param gameLogId
     * @return 200 ok
     */
    @PUT
    @Path("/tech/reveal/{gameLogId}")
    @Timed
    public Response revealTech(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("gameLogId") String gameLogId) {
        GameLogAction gameLogAction = new GameLogAction(db);
        GameLog gameLog = gameLogAction.findGameLogById(gameLogId);
        playerAction.revealTech(gameLog, pbfId, player.getId());
        return Response.ok().build();
    }

    //@DELETE doesn't work on angularjs with content-type
    @POST
    @Path("/item/discard")
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

    @POST
    @Path("/draw/{sheetName}")
    @Timed
    public Response drawItem(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("sheetName") String sheetNameString) {
        DrawAction drawAction = new DrawAction(db);

        Optional<SheetName> sheetNameOptional = SheetName.find(sheetNameString);
        if (!sheetNameOptional.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, player.getId(), sheetNameOptional.get());
        if (gameLogOptional.isPresent())
            return Response.ok().build();

        return Response.status(Response.Status.NOT_FOUND).build();
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
        DrawAction drawAction = new DrawAction(db);
        List<Unit> units = drawAction.drawUnitsFromForBattle(pbfId, player.getId(), numberOfunits);
        return Response.ok().entity(units).build();
    }

    /**
     * Draws barbarians
     *
     * @param player
     * @param pbfId
     * @return 200 ok
     */
    @PUT
    @Path("/battle/draw/barbarians")
    @Timed
    public Response drawBarbarians(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId) {
        DrawAction drawAction = new DrawAction(db);
        List<Unit> units = drawAction.drawBarbarians(pbfId, player.getId());
        return Response.ok().entity(units).build();
    }

    /**
     * Discard barbarians
     *
     * @param player
     * @param pbfId
     * @return 201 no content
     */
    @DELETE
    @Path("/battle/discard/barbarians")
    @Timed
    public Response discardBarbarians(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId) {
        DrawAction drawAction = new DrawAction(db);
        drawAction.discardBarbarians(pbfId, player.getId());
        return Response.noContent().build();
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
        DrawAction drawAction = new DrawAction(db);

        drawAction.endBattle(pbfId, player.getId());
        return Response.ok().build();
    }

    //TODO Perhaps no need for this. In this case we need to implement a new view with list of all votes

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
