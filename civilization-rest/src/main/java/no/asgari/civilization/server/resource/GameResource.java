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
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.GameDTO;
import no.asgari.civilization.server.dto.GameLogDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Tech;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
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
            log.error("GameId is missing");
            return Response.status(Response.Status.NOT_FOUND).build();
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
                .entity("{\"id\": " + id + "\"}")
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
     * Withdraw from existing game.
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
        if (ok) {
            return Response.ok().build();
        }

        log.warn("Cannot withdraw from game. Its already started");
        return Response.status(Response.Status.NOT_ACCEPTABLE)
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
                    .filter(log -> !Strings.isNullOrEmpty(log.getPublicLog()))
                    .map(gl -> new GameLogDTO(gl.getId(), gl.getPublicLog(), gl.getCreatedInMillis(), gl.getDraw()))
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
                    .filter(log -> log.getPrivateLog() != null && log.getPrivateLog().trim().isEmpty())
                    .map(gl -> new GameLogDTO(gl.getId(), gl.getPrivateLog(), gl.getCreatedInMillis(), gl.getDraw()))
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

    /**
     * Initiates undo for an item.
     * <p/>
     * Will throw BAD_REQUEST if undo has already been performed
     *
     * @param player
     * @param pbfId
     * @param gameLogId
     * @return 200 ok
     */
    @PUT
    @Path("/{pbfId}/undo/{gameLogId}")
    @Timed
    public Response undoItem(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("gameLogId") String gameLogId) {
        GameLogAction gameLogAction = new GameLogAction(db);
        GameLog gameLog = gameLogAction.findGameLogById(gameLogId);
        UndoAction undoAction = new UndoAction(db);
        undoAction.initiateUndo(gameLog, player.getId());
        return Response.ok().build();
    }

    /**
     * Performs yes vote on an undo
     * <p/>
     * Returns error if no undo is found
     *
     * @param player
     * @param pbfId
     * @param gameLogId
     * @return 200 ok
     */
    @PUT
    @Path("/{pbfId}/vote/{gameLogId}/yes")
    @Timed
    public Response voteYes(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("gameLogId") String gameLogId) {
        GameLogAction gameLogAction = new GameLogAction(db);
        GameLog gameLog = gameLogAction.findGameLogById(gameLogId);
        if (gameLog.getDraw() == null || gameLog.getDraw().getUndo() == null) {
            log.error("There is no undo to vote on");
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .build();
        }
        UndoAction undoAction = new UndoAction(db);
        undoAction.vote(gameLog, player.getId(), true);
        return Response.ok().build();
    }

    /**
     * Performs no vote on an undo
     * <p/>
     * Returns "412 Precondition failed" if no undo is found
     *
     * @param player
     * @param pbfId
     * @param gameLogId
     * @return 200 ok
     */
    @PUT
    @Path("/{pbfId}/vote/{gameLogId}/no")
    @Timed
    public Response voteNo(@Auth Player player, @NotEmpty @PathParam("pbfId") String pbfId, @NotEmpty @PathParam("gameLogId") String gameLogId) {
        GameLogAction gameLogAction = new GameLogAction(db);
        GameLog gameLog = gameLogAction.findGameLogById(gameLogId);
        if (gameLog.getDraw() == null || gameLog.getDraw().getUndo() == null) {
            log.error("There is no undo to vote on");
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .build();
        }
        UndoAction undoAction = new UndoAction(db);
        undoAction.vote(gameLog, player.getId(), false);
        return Response.ok().build();
    }

}
