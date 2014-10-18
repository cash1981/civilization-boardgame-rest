package no.asgari.civilization.server.action;

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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class that will perform draws and log them.
 * Draws will be saved in the gamelog collection
 */
@Log4j
public class DrawAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public DrawAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    /**
     * Finds the item or unit from the PBF matching the SheetName.
     * Then it removes the first item it finds
     * Draws item and units and puts the draw in the gamelog
     * @param pbfId - PBF id
     * @param playerId - Player id
     * @param sheetName - SheetName
     * @return
     */
    //TODO no need for optional really
    public Optional<GameLog> draw(String pbfId, String playerId, SheetName sheetName) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);
        Preconditions.checkNotNull(sheetName);

        checkYourTurn(pbfId, playerId);

        if (SheetName.TECHS.contains(sheetName)) {
            log.warn("Drawing of techs is not possible. You are supposed to choose a tech, not draw one");
            return Optional.empty();
        } else{
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

        //Items where empty, thus we need to populate more
        //TODO pbf.getItems().addAll(itemReader.readFromSheet(sheetName))


        return Optional.empty();
    }

    public List<Item> drawUnitsFromHand(String pbfId, String playerId, int numberOfDraws) {
        Playerhand playerhand = getPlayerhandFromPlayerId(playerId, pbfId);

        List<Item> unitsInHand = playerhand.getItems().stream()
                .filter(p -> SheetName.UNITS.contains(p.getSheetName()))
                .collect(Collectors.toList());

        if(unitsInHand.isEmpty()) {
            log.debug("Units to draw are empty");
            throw new WebApplicationException(Response.Status.NO_CONTENT);
        }

        if(unitsInHand.size() <= numberOfDraws) {
            createLog(unitsInHand, pbfId);
            return unitsInHand;
        }

        Collections.shuffle(unitsInHand);
        return unitsInHand.stream().limit(numberOfDraws)
                .peek(item -> createLog(item, pbfId, GameLog.LogType.ITEM))
                .collect(Collectors.toList());
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
