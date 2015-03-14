package no.asgari.civilization.server.resource;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import no.asgari.civilization.server.model.Player;

@Path("{pbfId}/draw")
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
}
