package no.asgari.civilization.server.resource;

import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.TurnAction;
import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.PlayerTurn;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.PublicPlayerTurn;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("{pbfId}/turn")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class TurnResource {
    private final DB db;
    private final TurnAction turnAction;

    public TurnResource(DB db) {
        this.db = db;
        turnAction = new TurnAction(db);
    }

    @GET
    @Path("/players")
    public List<String> getAllPlayers(@NotEmpty @PathParam("pbfId") String pbfId) {
        return turnAction.findPBFById(pbfId).getPlayers().stream()
                .map(Playerhand::getUsername)
                .sorted()
                .collect(Collectors.toList());
    }

    @GET
    public Collection<PlayerTurn> getPlayersturns(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId) {
        TurnAction turnAction = new TurnAction(db);
        return turnAction.getPlayersTurns(pbfId, player.getId());
    }

    @PUT
    @Path("/save")
    public Response saveTurn(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, TurnDTO turn) {
        TurnAction turnAction = new TurnAction(db);
        if("SOT".equalsIgnoreCase(turn.getPhase())) {
            turnAction.updateSOT(pbfId, player.getId(), turn);
        } else if("Trade".equalsIgnoreCase(turn.getPhase())) {
            turnAction.updateTrade(pbfId, player.getId(), turn);
        } else if("CM".equalsIgnoreCase(turn.getPhase())) {
            turnAction.updateCM(pbfId, player.getId(), turn);
        } else if("Movement".equalsIgnoreCase(turn.getPhase())) {
            turnAction.updateMovement(pbfId, player.getId(), turn);
        } else if("Research".equalsIgnoreCase(turn.getPhase())) {
            turnAction.updateResearch(pbfId, player.getId(), turn);
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("/reveal")
    public Response revealTurn(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId,
                               TurnDTO turn) {

        TurnAction turnAction = new TurnAction(db);
        Collection<PublicPlayerTurn> publicPlayerTurns = null;
        if("SOT".equalsIgnoreCase(turn.getPhase())) {
            publicPlayerTurns = turnAction.revealSOT(pbfId, player.getId(), turn);
        } else if("Trade".equalsIgnoreCase(turn.getPhase())) {
            publicPlayerTurns = turnAction.revealTrade(pbfId, player.getId(), turn);
        } else if("CM".equalsIgnoreCase(turn.getPhase())) {
            publicPlayerTurns = turnAction.revealCM(pbfId, player.getId(), turn);
        } else if("Movement".equalsIgnoreCase(turn.getPhase())) {
            publicPlayerTurns = turnAction.revealMovement(pbfId, player.getId(), turn);
        } else if("Research".equalsIgnoreCase(turn.getPhase())) {
            publicPlayerTurns = turnAction.revealResearch(pbfId, player.getId(), turn);
        }

        return Response.ok(publicPlayerTurns).build();
    }

    @PUT
    @Path("/lock")
    public Response lockOrUnlockTurn(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId,
                                     TurnDTO turn) {

        TurnAction turnAction = new TurnAction(db);
        Collection<PlayerTurn> playerTurns = turnAction.lockOrUnlockTurn(pbfId, player.getId(), turn);

        return Response.ok(playerTurns).build();
    }
}
