package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.email.SendEmail;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.PlayerTurn;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Turn;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;

@Log4j
public class TurnAction extends BaseAction {

    private final JacksonDBCollection<PBF, String> pbfCollection;

    public TurnAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public void updateAndLockSOT(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Set<PlayerTurn> playerturns = playerhand.getPlayerTurns();
        if(playerturns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn());
        }

        PlayerTurn playerTurn = getPlayerTurn(turnDTO, playerturns, playerhand.getUsername());
        playerTurn.getSotMap().put(turnDTO.getOrder(), turnDTO.isLocked());
        playerhand.getPlayerTurns().add(playerTurn);

        addTurnInPBF(turnDTO, pbf, playerhand, playerTurn);

        getListOfPlayersPlaying(pbfId)
                .stream()
                .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                .forEach(
                        p -> SendEmail.sendMessage(p.getEmail(), "Start of turn locked", playerhand.getUsername() + " has updated and locked start of turn with " +
                                "the following order\n:" + turnDTO.getOrder()
                                + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order")
                );

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.TRADE, playerId);

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.SOT, playerId);
    }

    public void updateAndLockTrade(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Set<PlayerTurn> playerturns = playerhand.getPlayerTurns();
        if(playerturns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn());
        }

        PlayerTurn playerTurn = getPlayerTurn(turnDTO, playerturns, playerhand.getUsername());

        playerTurn.getTradeMap().put(turnDTO.getOrder(), turnDTO.isLocked());

        addTurnInPBF(turnDTO, pbf, playerhand, playerTurn);

        getListOfPlayersPlaying(pbfId)
                .stream()
                .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                .forEach(
                        p -> SendEmail.sendMessage(p.getEmail(), "Trade locked", playerhand.getUsername() + " has updated and locked trade with " +
                                "the following order:\n" + turnDTO.getOrder()
                                + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order")
                );

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.TRADE, playerId);
    }

    public void updateAndLockCM(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Set<PlayerTurn> playerturns = playerhand.getPlayerTurns();
        if(playerturns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn());
        }

        PlayerTurn playerTurn = getPlayerTurn(turnDTO, playerturns, playerhand.getUsername());
        playerTurn.getCmMap().put(turnDTO.getOrder(), turnDTO.isLocked());

        addTurnInPBF(turnDTO, pbf, playerhand, playerTurn);

        getListOfPlayersPlaying(pbfId)
                .stream()
                .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                .forEach(
                        p -> SendEmail.sendMessage(p.getEmail(), "City management locked", playerhand.getUsername() + " has updated and locked city management with " +
                                "the following order:\n" + turnDTO.getOrder()
                                + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order")
                );

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.CM, playerId);
    }

    public void updateAndLockMovement(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Set<PlayerTurn> playerturns = playerhand.getPlayerTurns();
        if(playerturns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn());
        }

        PlayerTurn playerTurn = getPlayerTurn(turnDTO, playerturns, playerhand.getUsername());
        playerTurn.getMovementMap().put(turnDTO.getOrder(), turnDTO.isLocked());

        addTurnInPBF(turnDTO, pbf, playerhand, playerTurn);

        getListOfPlayersPlaying(pbfId)
                .stream()
                .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                .forEach(
                        p -> SendEmail.sendMessage(p.getEmail(), "Movement locked", playerhand.getUsername() + " has updated and locked movement with " +
                                "the following order:\n" + turnDTO.getOrder()
                                + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order")
                );

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.CM, playerId);
    }

    public void updateAndLockResearch(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Set<PlayerTurn> playerturns = playerhand.getPlayerTurns();
        if(playerturns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn());
        }

        PlayerTurn playerTurn = getPlayerTurn(turnDTO, playerturns, playerhand.getUsername());
        playerTurn.getResearchMap().put(turnDTO.getOrder(), turnDTO.isLocked());

        addTurnInPBF(turnDTO, pbf, playerhand, playerTurn);

        getListOfPlayersPlaying(pbfId)
                .stream()
                .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                .forEach(
                        p -> SendEmail.sendMessage(p.getEmail(), "Research locked", playerhand.getUsername() + " has updated and locked research with " +
                                "the following order:\n" + turnDTO.getOrder()
                                + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order")
                );

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.CM, playerId);
    }

    private void addTurnInPBF(TurnDTO turnDTO, PBF pbf, Playerhand playerhand, PlayerTurn playerTurn) {
        Optional<Turn> turnOptional = pbf.getTurnByUsernameAndTurnNr(playerhand.getUsername(), turnDTO.getTurnNumber());
        if(!turnOptional.isPresent()) {
            Turn turn = new Turn(playerTurn);
            pbf.getPublicTurns().add(turn);
        } else {
            Turn turn = turnOptional.get();
            turn.copy(playerTurn);
            pbf.getPublicTurns().add(turn);
        }
    }

    private PlayerTurn getPlayerTurn(TurnDTO turnDTO, Set<PlayerTurn> playerturns, String username) {
        PlayerTurn playerTurn = playerturns.stream()
                .filter(t -> t.getTurnNumber() == turnDTO.getTurnNumber())
                .findFirst()
                .orElse(new PlayerTurn(username, turnDTO.getTurnNumber()));
        playerTurn.setUsername(username);
        return playerTurn;
    }

}
