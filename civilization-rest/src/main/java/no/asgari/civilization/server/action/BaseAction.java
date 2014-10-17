package no.asgari.civilization.server.action;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;
import org.mongojack.JacksonDBCollection;

@Log4j
public abstract class BaseAction {
    protected final GameLogAction logAction;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    protected BaseAction(DB db) {
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.logAction = new GameLogAction(db);
    }

    /** Creates public and private logs of draws **/
    protected GameLog createLog(Draw<? extends Spreadsheet> draw, GameLog.LogType logType) {
        return logAction.createGameLog(draw,logType);
    }

    protected GameLog createLog(Tech chosenTech, String pbfId, GameLog.LogType logType) {
        return logAction.createGameLog(chosenTech, pbfId,logType);
    }

    protected PBF findPBFById(String pbfId) {
        try {
            return pbfCollection.findOneById(pbfId);
        } catch(Exception ex) {
            log.error("Couldn't find pbf");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot find pbf")
                    .build();
            throw new WebApplicationException(badReq);
        }
    }

    /**
     * Checks wheter is the players turn. If not UNAUTHORIZED exception is thrown
     * @param pbfId
     * @param playerId
     *
     * @throws WebApplicationException(Response) - Throws Response.UNAUTHORIZED if not your turn
     */
    //TODO Perhaps its best to have this in a filter, but its not always intended to be run
    void checkYourTurn(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandFromPlayerId(playerId, pbf);
        if (playerhand.isYourTurn()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity("Its not your turn")
                    .build());
        }
    }

    Playerhand getPlayerhandFromPlayerId(String playerId, PBF pbf) {
        return pbf.getPlayers()
                .stream().filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindPlayer);
    }

    Playerhand getPlayerhandFromPlayerId(String playerId, String pbfId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        return getPlayerhandFromPlayerId(playerId, pbf);
    }

    static WebApplicationException cannotFindItem() {
        return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity("Could not find item")
                .build());
    }

    static WebApplicationException cannotFindPlayer() {
        return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity("Could not find player")
                .build());
    }

}
