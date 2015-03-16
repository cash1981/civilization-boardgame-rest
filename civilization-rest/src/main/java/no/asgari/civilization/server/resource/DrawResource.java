package no.asgari.civilization.server.resource;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import com.codahale.metrics.annotation.Timed;
import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Unit;
import org.hibernate.validator.constraints.NotEmpty;

@Path("draw/{pbfId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log4j
public class DrawResource {

    private final DB db;
    @Context
    private UriInfo uriInfo;

    public DrawResource(DB db) {
        this.db = db;
    }

    /**
     * Rest endpoint that draws a random item from playerhand and gives to another player
     *
     * @param pbfId - The pbf id
     * @param targetPlayerId - The targeted player which will recieve the item
     * @param sheetName - the item to be automatically drawn from playerhand and given to another player
     * @param player - The logged in player that we will take the item from
     */
    @PUT
    @Timed
    @Path("/{sheetName}/loot/{targetPlayerId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response drawItemAndGiveToPlayer(@PathParam("pbfId") String pbfId, @PathParam("sheetName") String sheetName, @PathParam("targetPlayerId") String targetPlayerId, @Auth Player player) {
        DrawAction drawAction = new DrawAction(db);

        Optional<SheetName> sheetNameOptional = SheetName.find(sheetName);
        if (!sheetNameOptional.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Could not find item " + sheetName)
                    .build();
        }

        drawAction.drawRandomItemAndGiveToPlayer(pbfId, sheetNameOptional.get(), targetPlayerId, player.getId());
        return Response.ok().build();
    }

    @PUT
    @Timed
    @Path("/battlehand/reveal")
    public Response revealAndDiscardBattlehand(@PathParam("pbfId") String pbfId, @Auth Player player) {
        DrawAction drawAction = new DrawAction(db);
        drawAction.revealAndDiscardBattlehand(pbfId,player.getId());
        return Response.ok().build();
    }

    @POST
    @Path("/{sheetName}")
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
    @Path("/battle")
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
    @Path("/battle/barbarians")
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

}
