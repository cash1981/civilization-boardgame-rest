package no.asgari.civilization.server.action;

import java.util.Iterator;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import org.mongojack.JacksonDBCollection;

/**
 * Class that will perform draws and log them.
 * Draws will be saved in the gamelog collection
 */
@Log4j
public class DrawAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;

    public DrawAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    /**
     * Finds the item or unit from the PBF matching the SheetName.
     * Then it removes the first item it finds
     * Draws item and units and puts the draw in the gamelog
     * @param pbfId
     * @param playerId
     * @param sheetName
     * @return
     */
    public Optional<GameLog> draw(String pbfId, String playerId, SheetName sheetName) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);
        Preconditions.checkNotNull(sheetName);

        if (SheetName.TECHS.contains(sheetName)) {
            log.warn("Drawing of techs is not possible. You are supposed to choose a tech, not draw one");
            return Optional.empty();
        } else {
            checkYourTurn(pbfId, playerId);

            PBF pbf = pbfCollection.findOneById(pbfId);

            //Java 8 streamFromIterable doesn't support remove very well
            Iterator<Item> iterator = pbf.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getSheetName() == sheetName) {
                    putItemToPlayer(item, pbf, playerId);

                    iterator.remove();
                    item.setOwnerId(playerId);
                    pbfCollection.updateById(pbf.getId(), pbf);
                    log.debug("Drew item " + item + " and updated pbf");
                    Draw<Item> draw = createDraw(pbfId, playerId, item);
                    GameLog gamelog = createLog(draw, GameLog.LogType.ITEM);
                    return Optional.of(gamelog);
                }
            }
        }

        return Optional.empty();
    }

    private void putItemToPlayer(Item item, PBF pbf, String playerId) {
        Playerhand playerhand = getPlayerhandFromPlayerId(playerId, pbf);
        playerhand.getItems().add(item);
    }

    private static Draw<Item> createDraw(String pbfId, String playerId, Item item) {
        Draw<Item> draw = new Draw<>(pbfId, playerId);
        draw.setHidden(true);
        draw.setItem(item);
        return draw;
    }

}
