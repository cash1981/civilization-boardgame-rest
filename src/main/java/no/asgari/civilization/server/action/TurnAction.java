package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.misc.SecurityCheck;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.PlayerTurn;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Turn;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Log4j
public class TurnAction extends BaseAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;

    public TurnAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    public void updateAndLockSOT(String pbfId, String playerId, TurnDTO turnDTO) {
        PBF pbf = findPBFById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!SecurityCheck.hasUserAccess(pbf, playerId)) {
            log.error("User with id " + playerId + " has no access to pbf " + pbf.getName());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        List<PlayerTurn> playerturns = playerhand.getPlayerTurns();
        if(playerturns.isEmpty()) {
            playerhand.getPlayerTurns().add(new PlayerTurn());
        }

        PlayerTurn playerTurn = playerturns.stream()
                .filter(t -> t.getTurnNumber() == turnDTO.getTurnNumber())
                .findFirst()
                .orElseThrow(BaseAction::cannotFindItem);

        playerTurn.getSetupMap().put(turnDTO.getOrder(), turnDTO.isLocked());

        Optional<Turn> turnOptional = pbf.getTurnByUsernameAndTurnNr(playerhand.getUsername(), turnDTO.getTurnNumber());
        if(!turnOptional.isPresent()) {
            Turn turn = new Turn(playerTurn);
            pbf.getPublicTurns().add(turn);
        }

        Turn turn = turnOptional.get();
        turn.copy(playerTurn);

        pbfCollection.updateById(pbfId, pbf);
        super.createLog(pbfId, GameLog.LogType.SOT, playerId);
    }
}
