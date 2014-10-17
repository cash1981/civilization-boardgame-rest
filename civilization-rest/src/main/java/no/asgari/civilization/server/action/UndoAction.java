package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Undo;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Log4j
public class UndoAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;

    public UndoAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    public void putDrawnItemBackInPBF(PBF pbf, Draw draw) {
        Preconditions.checkNotNull(pbf);
        Preconditions.checkNotNull(draw);
        Preconditions.checkNotNull(draw.getItem());

        Item item = draw.getItem();

        Playerhand playerhand = getPlayerhandFromPlayerId(draw.getPlayerId(), pbf);
        if(item instanceof Tech) {
            //Remove from tech
            boolean remove = playerhand.getTechsChosen().remove(item);
            if(remove) log.debug("Successfully undoed tech");
            else log.error("Didn't find tech to remove from playerhand: " + item);
        } else {
            pbf.getItems().add(item);
            boolean remove = playerhand.getItems().remove(item);
            if(remove) log.debug("Successfully undoed item");
            else log.error("Didn't find item to remove from playerhand: " + item);
        }
        pbfCollection.updateById(pbf.getId(), pbf);
    }

    /**
     * Will perform vote, and if all votes are successfull, item is put back in the deck
     *
     * A vote must have already been initiated
     * @param gameLog
     * @param playerId
     * @param vote
     * @return
     */
    public GameLog vote(GameLog gameLog, String playerId, boolean vote) {
        Preconditions.checkNotNull(gameLog);
        Preconditions.checkNotNull(gameLog.getDraw());
        Preconditions.checkNotNull(gameLog.getDraw().getUndo());

        PBF pbf = pbfCollection.findOneById(gameLog.getPbfId());
        if(!pbf.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId))) {
            log.error("Couldn't find playerId " + playerId + " in PBF's players");
            throw new IllegalArgumentException("Wrong playerId");
        }
        gameLog.getDraw().getUndo().vote(playerId,vote);
        gameLogCollection.updateById(gameLog.getId(), gameLog);

        Optional<Boolean> resultOfVotes = gameLog.getDraw().getUndo().getResultOfVotes();
        if(resultOfVotes.isPresent() && resultOfVotes.get()) {
            log.info("Everyone has performed a vote, so we put item back in the deck");
            putDrawnItemBackInPBF(pbf, gameLog.getDraw());
        }
        return gameLog;
    }

    /**
     * Will request for undo and initiate a vote
     * @param gameLog
     * @param playerId
     * @return
     */
    //TODO Will need to find out how to initate the vote (Perhaps search for all game logs which have unfinished undo
    public void initiateUndo(GameLog gameLog, String playerId) {
        Preconditions.checkNotNull(gameLog);
        Preconditions.checkNotNull(gameLog.getDraw());

        Draw<?> draw = gameLog.getDraw();
        if(draw.getUndo() != null) {
            log.error("Cannot initiate a undo. Its already been initiated");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Undo already initiated")
                    .build());
        }
        PBF pbf = pbfCollection.findOneById(gameLog.getPbfId());
        if(!pbf.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId))) {
            log.error("Couldn't find playerId " + playerId + " in PBF's players");
            throw new IllegalArgumentException("Wrong playerId");
        }

        draw.setUndo(new Undo(pbf.getNumOfPlayers()));
        draw.getUndo().vote(playerId, true);
        gameLogCollection.updateById(gameLog.getId(), gameLog);
    }
}
