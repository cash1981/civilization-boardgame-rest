package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.exception.NoMoreItemsException;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Unit;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Class that will perform draws and log them.
 * Draws will be saved in the gamelog collection
 */
@Log4j
public class DrawAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final GameLogAction gameLogAction;

    public DrawAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        gameLogAction = new GameLogAction(db);
    }

    public Optional<GameLog> draw(String pbfId, String playerId, SheetName sheetName, GameType gameType) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);
        Preconditions.checkNotNull(sheetName);
        Preconditions.checkNotNull(gameType);

        checkYourTurn(pbfId, playerId);
        PBF pbf = pbfCollection.findOneById(pbfId);

        if (SheetName.TECHS.contains(sheetName)) {
            log.warn("Drawing of techs is not possible. Techs are supposed to be chosen, not drawn.");
            return Optional.empty();
        } else {
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

        log.warn("No more " + sheetName.getName() + " to draw. Possibly no more items left to draw in the deck. Will try to reshuffle");

        reshuffleItems(gameType, sheetName, pbf);
        //Recursion call (should only be called once, because shuffle is made)
        return draw(pbfId, playerId, sheetName, gameType);
    }

    private void reshuffleItems(GameType gameType, SheetName sheetName, PBF pbf) {
        if(!SheetName.SHUFFLABLE_ITEMS.contains(sheetName)) {
            log.warn("Tried to reshuffle " + sheetName.getName() + " but not a shufflable type");
            return;
        }
        try {
            ItemReader itemReader = CivSingleton.instance().itemsCache().get(gameType);

            List<Item> itemsFromExcel = itemReader.redrawableItems.parallelStream()
                    .filter(s -> s.getSheetName() == sheetName)
                    .collect(Collectors.toList());

            //Find the items in use by all players
            List<Item> playersItem = pbf.getPlayers().stream()
                    .flatMap(p -> p.getItems().stream())
                    .filter(it -> it.getSheetName() == sheetName)
                    .collect(Collectors.toList());

            log.debug("Not adding these items which are in use " + playersItem);
            itemsFromExcel.removeAll(playersItem);
            if(itemsFromExcel.size() == 0) {
                log.warn("All items are still in use, cannot make a shuffle");
                throw new NoMoreItemsException(sheetName.getName());
            }

            log.debug("Shuffling, and adding items back in the pbf");
            Collections.shuffle(itemsFromExcel);
            pbf.getItems().addAll(itemsFromExcel);
            pbfCollection.updateById(pbf.getId(), pbf);

            logShuffle(sheetName, pbf);

        } catch (ExecutionException e) {
            log.error("Couldn't get itemcache by " + gameType, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
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
    public Optional<GameLog> draw(String pbfId, String playerId, SheetName sheetName) {
        return draw(pbfId,playerId,sheetName, GameType.WAW);
    }

    private void logShuffle(SheetName sheetName, PBF pbf) {
        GameLog log = new GameLog();
        log.setUsername("System");
        log.setPbfId(pbf.getId());
        log.setPublicLog(sheetName.getName() + " reshuffled and put back in the deck");
        gameLogAction.save(log);
    }

    private void putItemToPlayer(Item item, PBF pbf, String playerId) {
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        playerhand.addItem(item);
    }

    private static Draw<Item> createDraw(String pbfId, String playerId, Item item) {
        Draw<Item> draw = new Draw<>(pbfId, playerId);
        draw.setHidden(true);
        draw.setItem(item);
        return draw;
    }
}
