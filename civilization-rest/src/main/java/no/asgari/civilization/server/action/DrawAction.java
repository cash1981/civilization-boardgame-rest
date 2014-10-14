package no.asgari.civilization.server.action;

import java.util.Iterator;
import java.util.Optional;

import com.codahale.metrics.MetricRegistryListener;
import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Spreadsheet;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

/**
 * Class that will perform draws and log them
 */
@Log4j
public class DrawAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Draw, String> drawCollection;
    private final JacksonDBCollection<Player, String> playerCollection;

    public DrawAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.drawCollection = JacksonDBCollection.wrap(db.getCollection(Draw.COL_NAME), Draw.class, String.class);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
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

}
