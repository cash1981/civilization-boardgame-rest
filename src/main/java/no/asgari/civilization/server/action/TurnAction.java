package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.email.SendEmail;
import no.asgari.civilization.server.misc.CivUtil;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.PlayerTurn;
import no.asgari.civilization.server.model.Playerhand;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Log4j
public class TurnAction extends BaseAction {

    private final JacksonDBCollection<PBF, String> pbfCollection;

    public TurnAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public void updateSOT(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        saveTurn(playerId, turnDTO, pbf, playerhand);

        Thread thread = new Thread(() -> {
            pbf.getPlayers()
                    .stream()
                    .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                    .filter(CivUtil::shouldSendEmailInGame)
                    .forEach(p -> SendEmail.sendMessage(p.getEmail(), "Start of turn updated", playerhand.getUsername() + " has updated start of turn with " +
                                    "the following order\n:" + turnDTO.getOrder()
                                    + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order", playerhand.getPlayerId())
                    );
        });
        thread.start();

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.SOT, playerId);
    }

    public void updateTrade(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        saveTurn(playerId, turnDTO, pbf, playerhand);

        Thread thread = new Thread(() -> {
            pbf.getPlayers()
                    .stream()
                    .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                    .filter(CivUtil::shouldSendEmailInGame)
                    .forEach(p -> SendEmail.sendMessage(p.getEmail(), "Trade updated", playerhand.getUsername() + " has updated trade with " +
                                    "the following order:\n" + turnDTO.getOrder()
                                    + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order", playerhand.getPlayerId())
                    );
        });
        thread.start();

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.TRADE, playerId);
    }

    public void updateCM(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        saveTurn(playerId, turnDTO, pbf, playerhand);

        Thread thread = new Thread(() -> {
            pbf.getPlayers()
                    .stream()
                    .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                    .filter(CivUtil::shouldSendEmailInGame)
                    .forEach(p -> {
                                SendEmail.sendMessage(p.getEmail(), "City management updated", playerhand.getUsername() + " has updated city management with " +
                                        "the following order:\n" + turnDTO.getOrder()
                                        + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order", playerhand.getPlayerId());
                            }
                    );
        });
        thread.start();

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.CM, playerId);
    }

    public void updateMovement(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        saveTurn(playerId, turnDTO, pbf, playerhand);

        Thread thread = new Thread(() -> {
            pbf.getPlayers()
                    .stream()
                    .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                    .filter(CivUtil::shouldSendEmailInGame)
                    .forEach(
                            p -> SendEmail.sendMessage(p.getEmail(), "Movement updated", playerhand.getUsername() + " has updated movement with " +
                                    "the following order:\n" + turnDTO.getOrder()
                                    + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order", playerhand.getPlayerId())
                    );
        });
        thread.start();

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.MOVEMENT, playerId);
    }

    public void updateResearch(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        saveTurn(playerId, turnDTO, pbf, playerhand);

        Thread thread = new Thread(() -> {
            pbf.getPlayers()
                    .stream()
                    .filter(p -> !p.getUsername().equals(playerhand.getUsername()))
                    .filter(CivUtil::shouldSendEmailInGame)
                    .forEach(p -> SendEmail.sendMessage(p.getEmail(), "Research updated", playerhand.getUsername() + " has updated research with " +
                                    "the following order:\n" + turnDTO.getOrder()
                                    + ".\n\nLogin to " + SendEmail.gamelink(pbfId) + " to see the order", playerhand.getPlayerId())
                    );
        });
        thread.start();

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.RESEARCH, playerId);
    }

    /**
     * Each player can add a new turn so they can start new to write new orders
     */
    public void addNewTurn(String pbfId, String playerId, int turnNumber) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);

        Optional<PlayerTurn> turnOptional = playerhand.getPlayerTurns().stream()
                .filter(p -> p.getTurnNumber() == turnNumber)
                .findAny();

        if (!turnOptional.isPresent()) {
            playerhand.getPlayerTurns().add(new PlayerTurn(playerhand.getUsername(), turnNumber));
        }
    }

    /**
     * If none is found, a new one is created but not saved
     * @param turnNumber
     * @param playerhand
     * @return
     */
    private PlayerTurn getPlayerTurnByTurnNumber(int turnNumber, Playerhand playerhand) {
        PlayerTurn playerTurn = playerhand.getPlayerTurns().stream()
                .filter(t -> t.getTurnNumber() == turnNumber)
                .findFirst()
                .orElse(new PlayerTurn(playerhand.getUsername(), turnNumber));
        //playerTurn.setUsername(username);
        return playerTurn;
    }

    public List<PlayerTurn> getAllPublicTurns(String pbfId) {
        PBF pbf = findPBFById(pbfId);

        return pbf.getPublicTurns().values().stream()
                .sorted()
                .map(p -> {
                    //Remove the last order in the history
                    p.getSotHistory().remove(p.getSot());
                    p.getTradeHistory().remove(p.getTrade());
                    p.getCmHistory().remove(p.getCm());
                    p.getMovementHistory().remove(p.getMovement());
                    p.getResearchHistory().remove(p.getResearch());
                    return p;
                })
                .collect(toList());

    }

    /**
     * Gets all players turns.
     * If there are none, it will create one with turn number 1
     * @param pbfId
     * @param playerId
     * @return
     */
    public Set<PlayerTurn> getPlayersTurns(String pbfId, String playerId) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        Set<PlayerTurn> playerTurns = playerhand.getPlayerTurns();
        if (playerTurns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn(playerhand.getUsername(), 1));
            pbfCollection.updateById(pbfId, pbf);
        }

        return playerTurns;
    }

    private void saveTurn(String playerId, TurnDTO turnDTO, PBF pbf, Playerhand playerhand) {
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        PlayerTurn pt = getPlayerTurnByTurnNumber(turnDTO.getTurnNumber(), playerhand);

        if ("SOT".equalsIgnoreCase(turnDTO.getPhase())) {
            pt.setSot(turnDTO.getOrder());
            pt.getSotHistory().add(turnDTO.getOrder());
        } else if ("Trade".equalsIgnoreCase(turnDTO.getPhase())) {
            pt.setTrade(turnDTO.getOrder());
            pt.getTradeHistory().add(turnDTO.getOrder());
        } else if ("CM".equalsIgnoreCase(turnDTO.getPhase())) {
            pt.setCm(turnDTO.getOrder());
            pt.getCmHistory().add(turnDTO.getOrder());
        } else if ("Movement".equalsIgnoreCase(turnDTO.getPhase())) {
            pt.setMovement(turnDTO.getOrder());
            pt.getMovementHistory().add(turnDTO.getOrder());
        } else if ("Research".equalsIgnoreCase(turnDTO.getPhase())) {
            pt.setResearch(turnDTO.getOrder());
            pt.getResearchHistory().add(turnDTO.getOrder());
        }
        playerhand.getPlayerTurns().add(pt);
        pbfCollection.updateById(pbf.getId(), pbf);
    }

    public Collection<PlayerTurn> addTurnInPBF(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        PlayerTurn playerTurn = getPlayerTurnByTurnNumber(turnDTO.getTurnNumber(), playerhand);
        // Cant get map serialization to work in jackson
        //final TurnKey turnKey = new TurnKey(playerTurn.getTurnNumber(), playerTurn.getUsername());
        pbf.getPublicTurns().put(playerTurn.getTurnNumber() + playerTurn.getUsername(), playerTurn);
        pbfCollection.updateById(pbf.getId(), pbf);
        return pbf.getPublicTurns().values();
    }

    public Set<PlayerTurn> lockOrUnlockTurn(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        PlayerTurn playerTurn = playerhand.getPlayerTurns().stream()
                .filter(p -> p.getTurnNumber() == turnDTO.getTurnNumber())
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem);

        playerTurn.setDisabled(turnDTO.isLocked());
        String message = turnDTO.isLocked() ? " has locked in turn " + turnDTO.getTurnNumber() : " has re-opened turn " + turnDTO.getTurnNumber();
        createCommonPublicLog(message, pbfId, playerId);

        if(turnDTO.isLocked() && !playerhand.getPlayerTurns().stream().anyMatch(p -> p.getTurnNumber() == (turnDTO.getTurnNumber()+1))) {
            //create new turn
            playerhand.getPlayerTurns().add(new PlayerTurn(playerhand.getUsername(), turnDTO.getTurnNumber() + 1));
        }

        pbfCollection.updateById(pbfId, pbf);

        return getPlayersTurns(pbfId, playerId);
    }
}
