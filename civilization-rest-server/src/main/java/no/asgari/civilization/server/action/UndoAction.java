package no.asgari.civilization.server.action;

import lombok.RequiredArgsConstructor;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Undo;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

@RequiredArgsConstructor
public class UndoAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Draw, String> drawCollection;

    /**
     * All must agree for undo to be performed.
     * If absent/empty, then all players have not voted
     */
    public Optional<Boolean> getResultOfVotes(Undo undo) {
        Draw draw = drawCollection.findOneById(undo.getDrawId());
        PBF pbf = pbfCollection.findOneById(draw.getPbfId());

        if(undo.getVotes().size() < pbf.getNumOfPlayers()) {
            //Not everyone has voted yet
            return Optional.empty();
        }

        if(undo.getVotes().containsValue(Boolean.FALSE)) {
            return Optional.of(Boolean.FALSE);
        }

        return Optional.of(Boolean.TRUE);
    }


    public int votesRemaining(Undo undo) {
        PBF pbf = findPBF(undo);
        return Math.abs(undo.getVotes().size() - pbf.getNumOfPlayers());
    }

    private PBF findPBF(Undo undo) {
        Draw draw = drawCollection.findOneById(undo.getDrawId());
        return pbfCollection.findOneById(draw.getPbfId());
    }

}
