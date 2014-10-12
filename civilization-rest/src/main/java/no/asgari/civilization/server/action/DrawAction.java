package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.model.Spreadsheet;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Class that will perform draws and log them
 */
@Log4j
public class DrawAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Draw, String> drawCollection;
    private final JacksonDBCollection<Player, String> playerCollection;
    private final GameLogAction logAction;

    public DrawAction(DB db) {
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.drawCollection = JacksonDBCollection.wrap(db.getCollection(Draw.COL_NAME), Draw.class, String.class);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        logAction = new GameLogAction(db);
    }

    /**
     * Finds the item or unit from the PBF matching the SheetName.
     * Then it removes the first item it finds
     * Draws item and units and creates a Draw entry in the Draw collection as well as private and public logs
     * @param pbfId
     * @param playerId
     * @param sheetName
     * @return
     */
    public Optional<Draw<? extends Spreadsheet>> draw(String pbfId, String playerId, SheetName sheetName) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);
        Preconditions.checkNotNull(sheetName);
        PBF pbf = pbfCollection.findOneById(pbfId);

        //Java 8 stream doesn't support remove very well
        Iterator<Spreadsheet> iterator = pbf.getItems().iterator();
        while(iterator.hasNext()) {
            Spreadsheet spreadsheet = iterator.next();
            if(spreadsheet.getSheetName() == sheetName) {
                Draw<Spreadsheet> draw = new Draw<>(pbfId, playerId);
                draw.setItem(spreadsheet);
                iterator.remove();
                WriteResult<Draw, String> drawInsert = drawCollection.insert(draw);
                pbfCollection.updateById(pbf.getId(), pbf);
                log.debug("Drew item " + spreadsheet + " and updated pbf");
                createLog(draw);
                draw.setId(drawInsert.getSavedId());
                return Optional.of(draw);
            }
        }

        return Optional.empty();
    }

    private void createLog(Draw<? extends Spreadsheet> draw) {
        createPublicLog(draw);
        createPrivateLog(draw);
    }

    private PublicLog createPublicLog(Draw draw) {
        PublicLog pl = new PublicLog();
        pl.setDraw(draw);
        pl.setPbfId(draw.getPbfId());
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.createAndSetLog();
        pl.setId(logAction.record(pl));
        return pl;
    }

    private PrivateLog createPrivateLog(Draw draw) {
        PrivateLog pl = new PrivateLog();
        pl.setDraw(draw);
        pl.setPbfId(draw.getPbfId());
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.setReveal(false);
        pl.createAndSetLog();
        pl.setId(logAction.record(pl));
        return pl;
    }

    private String getUsernameFromPlayerId(String playerId) {
        return playerCollection.findOneById(playerId).getUsername();
    }

}
