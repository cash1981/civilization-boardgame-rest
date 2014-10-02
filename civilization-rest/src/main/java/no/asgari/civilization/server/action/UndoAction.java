package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Aircraft;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Citystate;
import no.asgari.civilization.server.model.Civ;
import no.asgari.civilization.server.model.CultureI;
import no.asgari.civilization.server.model.CultureII;
import no.asgari.civilization.server.model.CultureIII;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.model.Hut;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Mounted;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Tile;
import no.asgari.civilization.server.model.Type;
import no.asgari.civilization.server.model.Undo;
import no.asgari.civilization.server.model.Village;
import no.asgari.civilization.server.model.Wonder;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

@Log4j
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

    public void putDrawnItemBackInPBF(Draw draw) {
        Preconditions.checkNotNull(draw);
        Preconditions.checkNotNull(draw.getItem());

        final Type item = draw.getItem();
        final PBF pbf = pbfCollection.findOneById(draw.getPbfId());

        if(item instanceof Civ) {
            log.info("Putting civ " + item.getType() + " back in the collection");
            pbf.getCivs().add((Civ) item);
        } else if(item instanceof Aircraft) {
            log.info("Putting Aircraft " + item.getType() + " back in the collection");
            pbf.getAircraft().add((Aircraft) item);
        }  else if(item instanceof Artillery) {
            log.info("Putting Artillery " + item.getType() + " back in the collection");
            pbf.getArtillery().add((Artillery) item);
        } else if(item instanceof Citystate) {
            log.info("Putting Citystate " + item.getType() + " back in the collection");
            pbf.getCitystates().add((Citystate) item);
        } else if(item instanceof CultureI) {
            log.info("Putting CultureI " + item.getType() + " back in the collection");
            pbf.getCultureIs().add((CultureI) item);
        } else if(item instanceof CultureII) {
            log.info("Putting CultureII " + item.getType() + " back in the collection");
            pbf.getCultureIIs().add((CultureII) item);
        } else if(item instanceof CultureIII) {
            log.info("Putting CultureIII " + item.getType() + " back in the collection");
            pbf.getCultureIIIs().add((CultureIII) item);
        } else if(item instanceof GreatPerson) {
            log.info("Putting GreatPerson " + item.getType() + " back in the collection");
            pbf.getGreatPersons().add((GreatPerson) item);
        } else if(item instanceof Hut) {
            log.info("Putting Hut " + item.getType() + " back in the collection");
            pbf.getHuts().add((Hut) item);
        } else if(item instanceof Infantry) {
            log.info("Putting Infantry " + item.getType() + " back in the collection");
            pbf.getInfantry().add((Infantry) item);
        } else if(item instanceof Mounted) {
            log.info("Putting Mounted " + item.getType() + " back in the collection");
            pbf.getMounted().add((Mounted) item);
        } else if(item instanceof Tile) {
            log.info("Putting Tile " + item.getType() + " back in the collection");
            pbf.getTiles().add((Tile) item);
        } else if(item instanceof Village) {
            log.info("Putting Village " + item.getType() + " back in the collection");
            pbf.getVillages().add((Village) item);
        } else if(item instanceof Wonder) {
            log.info("Putting Wonder " + item.getType() + " back in the collection");
            pbf.getWonders().add((Wonder) item);
        }

        pbfCollection.updateById(pbf.getId(), pbf);

    }



}
