package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Undo;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

@Log4j
public class UndoAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;

    public UndoAction(DB db) {
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    public Optional<Boolean> getResultOfVotes(GameLog gameLog) {
        return getResultOfVotes(gameLog.getDraw());
    }
    /**
     * All must agree for draw to be performed.
     * If absent/empty, then all players have not voted
     * @param draw
     */
    public Optional<Boolean> getResultOfVotes(Draw draw) {
        PBF pbf = pbfCollection.findOneById(draw.getPbfId());

        if(draw.getUndo() == null || draw.getUndo().getVotes().size() < pbf.getNumOfPlayers()) {
            //No draw initiated or not enough votes
            return Optional.empty();
        }

        if(draw.getUndo().getVotes().containsValue(Boolean.FALSE)) {
            return Optional.of(Boolean.FALSE);
        }

        return Optional.of(Boolean.TRUE);
    }

    private PBF findPBF(Draw draw) {
        return pbfCollection.findOneById(draw.getPbfId());
    }

    public void putDrawnItemBackInPBF(Draw draw) {
        Preconditions.checkNotNull(draw);
        Preconditions.checkNotNull(draw.getItem());

        final Spreadsheet item = draw.getItem();
        final PBF pbf = pbfCollection.findOneById(draw.getPbfId());
        pbf.getItems().add(item);
        pbfCollection.updateById(pbf.getId(), pbf);
    }

    public GameLog vote(GameLog gameLog, String playerId, boolean vote) {
        Preconditions.checkNotNull(gameLog);
        Preconditions.checkNotNull(gameLog.getDraw());

        PBF pbf = pbfCollection.findOneById(gameLog.getPbfId());
        if(!pbf.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId))) {
            log.error("Couldn't find playerId " + playerId + " in PBF's players");
            throw new IllegalArgumentException("Wrong playerId");
        }
        if(gameLog.getDraw().getUndo() == null) {
            gameLog.getDraw().setUndo(new Undo(pbf.getNumOfPlayers()));
        }
        gameLog.getDraw().getUndo().vote(playerId,vote);

        gameLogCollection.updateById(gameLog.getId(), gameLog);
        return gameLog;
    }
}
