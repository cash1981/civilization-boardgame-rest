package no.asgari.civilization.server.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DB;
import io.dropwizard.auth.Auth;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.action.GameLogAction;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.dto.*;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Tech;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log4j
public class GameResource {
    private final DB db;
    @Context
    private UriInfo uriInfo;

    public GameResource(DB db) {
        this.db = db;
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
        GameAction gameAction = new GameAction(db);
        List<PbfDTO> games = gameAction.getAllActiveGames();

        return Response.ok()
                .entity(games)
                .build();
    }

    /**
     * Returns a specific game
     *
     * @return
     */
    @Path("/{gameId}")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGame(@Auth(required = false) Player player, @PathParam("gameId") String pbfId) {
        if (Strings.isNullOrEmpty(pbfId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("GameId is missing").build();
        }
        GameAction gameAction = new GameAction(db);
        PBF pbf = gameAction.findPBFById(pbfId);
        GameDTO gameDTO = gameAction.getGame(pbf, player);

        return Response.ok()
                .entity(gameDTO)
                .build();
    }

    /**
     * Will return a collection of all pbf ids
     */
    @Path("/player")
    @GET
    @Timed
    public Response getGamesByPlayer(@Auth Player player) {
        PlayerAction playerAction = new PlayerAction(db);
        Set<String> games = playerAction.getGames(player);
        //TODO Perhaps nice to create location for all the games
        return Response.ok().entity(games).build();
    }

    /**
     * Will return a list of all the players of this PBF.
     * Handy for selecting players whom to trade with
     */
    @GET
    @Path("/{pbfId}/players")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPlayersForPBF(@NotEmpty @PathParam("pbfId") String pbfId) {
        GameAction gameAction = new GameAction(db);
        List<PlayerDTO> players = gameAction.getAllPlayers(pbfId);

        return Response.ok()
                .entity(players)
                .build();
    }

    @POST
    @Timed
    public Response createGame(@Valid CreateNewGameDTO dto, @Auth Player player) {
        Preconditions.checkNotNull(dto);
        Preconditions.checkNotNull(player);

        log.info("Creating game " + dto);
        GameAction gameAction = new GameAction(db);
        String id = gameAction.createNewGame(dto, player.getId());
        return Response.status(Response.Status.CREATED)
                .location(uriInfo.getAbsolutePathBuilder().path(id).build())
                .entity(id)
                .build();
    }

    @PUT
    @Timed
    @Path("/{pbfId}/join")
    public Response joinGame(@NotEmpty @PathParam("pbfId") String pbfId, @Auth Player player) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(player);

        GameAction gameAction = new GameAction(db);
        gameAction.joinGame(pbfId, player.getId());
        return Response.ok().build();
    }

    /**
     * With draw from exisiting game.
     * Game must not have started
     */
    @PUT
    @Timed
    @Path("/{pbfId}/withdraw")
    public Response withdrawFromGame(@NotEmpty @PathParam("pbfId") String pbfId, @Auth Player player) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(player);

        GameAction gameAction = new GameAction(db);
        boolean ok = gameAction.withdrawFromGame(pbfId, player.getId());
        if(ok) {
            return Response.ok().build();
        }

        return Response.notModified()
                .entity("Cannot withdraw from game. It is already started")
                .location(uriInfo.getAbsolutePathBuilder().path(pbfId).build())
                .build();
    }

    /**
     * Gets all the available techs. Will remove the techs that player already have chosen
     *
     * @param pbfId  - The PBF
     * @param player - The Authenticated player
     * @return - Response ok with a list of techs
     */
    @GET
    @Timed
    @Path("/{pbfId}/techs")
    public List<Tech> getAvailableTechs(@NotEmpty @PathParam("pbfId") String pbfId, @Auth Player player) {
        //TODO Change return type to TechDTO or use ItemDTO
        return new PlayerAction(db).getRemaingTechsForPlayer(player.getId(), pbfId);
    }

    @GET
    @Timed
    @Path("/{pbfId}/publiclog")
    public List<GameLogDTO> getPublicLog(@NotEmpty @PathParam("pbfId") String pbfId) {
        GameLogAction gameLogAction = new GameLogAction(db);
        List<GameLog> allPublicLogs = gameLogAction.getGameLogs(pbfId);
        List<GameLogDTO> gameLogDTOs = new ArrayList<>();
        if (!allPublicLogs.isEmpty()) {
            gameLogDTOs = allPublicLogs.stream()
                    .map(gl -> new GameLogDTO(gl.getId(), gl.getPublicLog(), gl.getCreatedInMillis()))
                    .collect(Collectors.toList());
        }
        return gameLogDTOs;
    }

    @GET
    @Timed
    @Path("/{pbfId}/privatelog")
    public List<GameLogDTO> getPrivateLog(@NotEmpty @PathParam("pbfId") String pbfId, @Auth Player player) {
        GameLogAction gameLogAction = new GameLogAction(db);

        List<GameLog> allPrivateLogs = gameLogAction.getGameLogsBelongingToPlayer(pbfId, player.getUsername());
        List<GameLogDTO> gameLogDTOs = new ArrayList<>();
        if (!allPrivateLogs.isEmpty()) {
            gameLogDTOs = allPrivateLogs.stream()
                    .map(gl -> new GameLogDTO(gl.getId(), gl.getPrivateLog(), gl.getCreatedInMillis() ))
                    .collect(Collectors.toList());
        }
        return gameLogDTOs;
    }

    /**
     * Returns a list of all undoes that are currently initiated and still not finished
     *
     * @param pbfId
     * @return
     */
    @GET
    @Path("/{pbfId}/undo/active")
    @Timed
    public Response getAllActiveUndosCurrentlyInProgress(@NotEmpty @PathParam("pbfId") String pbfId) {
        UndoAction undoAction = new UndoAction(db);
        List<GameLog> gamelogs = undoAction.getAllActiveUndos(pbfId);
        return Response.ok().entity(gamelogs).build();
    }

    /**
     * Returns a list of all undoes that are finished voted
     *
     * @param pbfId
     * @return
     */
    @GET
    @Path("/{pbfId}/undo/finished")
    @Timed
    public Response getAllFinishedUndos(@NotEmpty @PathParam("pbfId") String pbfId) {
        UndoAction undoAction = new UndoAction(db);
        List<GameLog> gamelogs = undoAction.getAllFinishedUndos(pbfId);
        return Response.ok().entity(gamelogs).build();
    }

}
