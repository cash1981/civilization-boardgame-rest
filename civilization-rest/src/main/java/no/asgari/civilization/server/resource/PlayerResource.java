package no.asgari.civilization.server.resource;

import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

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
import java.util.List;

/**
 * Contains player specific resources
 */
@Path("player")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class PlayerResource {
    @Context
    private UriInfo uriInfo;

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public PlayerResource(JacksonDBCollection<Player, String> playerCollection, JacksonDBCollection<PBF, String> pbfCollection) {
        this.playerCollection = playerCollection;
        this.pbfCollection = pbfCollection;
    }

    @GET
    public Response getGamesForPlayer(@Auth Player player) {
        PlayerAction playerAction = new PlayerAction(playerCollection, pbfCollection);
        List<PBF> games = playerAction.getGames(player);
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
        PlayerAction playerAction = new PlayerAction(playerCollection, pbfCollection);
        playerAction.chooseTech(pbfId, item, player.getUsername());
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
        PlayerAction playerAction = new PlayerAction(playerCollection, pbfCollection);
        boolean success = playerAction.endTurn(pbfId, player.getUsername());

        if(success) return Response.noContent().build();

        return Response.serverError().build();

    }

}
