/*
 * Copyright (c) 2015 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.MessageDTO;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.exception.NoMoreItemsException;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tradable;
import no.asgari.civilization.server.model.Unit;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class that will perform draws and log them.
 * Draws will be saved in the gamelog collection
 */
@Log4j
public class DrawAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final GameLogAction gameLogAction;

    private final StringBuilder sb = new StringBuilder();
    private final Consumer<Unit> revealUnitConsumer = unit -> sb.append(unit.revealAll()).append(", ");

    public DrawAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        gameLogAction = new GameLogAction(db);
    }

    public Optional<GameLog> draw(String pbfId, String playerId, SheetName sheetName) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);
        Preconditions.checkNotNull(sheetName);

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

        reshuffleItems(sheetName, pbf);
        //Recursion call (should only be called once, because shuffle is made)
        return draw(pbfId, playerId, sheetName);
    }

    private void reshuffleItems(SheetName sheetName, PBF pbf) {
        if (!SheetName.SHUFFLABLE_ITEMS.contains(sheetName)) {
            log.warn("Tried to reshuffle " + sheetName.getName() + " but not a shufflable type");
            throw new NoMoreItemsException(sheetName.getName());
        }
        ItemReader itemReader;
        try {
            itemReader = CivSingleton.instance().itemsCache().get(pbf.getType());
        } catch (ExecutionException e) {
            log.error("Couldnt get itemReader from cache " + e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

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

        //Now add the discarded items
        List<Item> discardedItems = pbf.getDiscardedItems().stream()
                .filter(it -> it.getSheetName() == sheetName)
                .collect(Collectors.toList());
        log.debug("Found " + discardedItems.size() + " discarded items of type " + sheetName + " to add in the deck");
        itemsFromExcel.addAll(discardedItems);

        if (itemsFromExcel.size() == 0) {
            log.warn("All items are still in use, cannot make a shuffle. Nothing to draw!");
            throw new NoMoreItemsException(sheetName.getName());
        }

        log.debug("Shuffling, and adding items back in the pbf");
        Collections.shuffle(itemsFromExcel);
        pbf.getItems().addAll(itemsFromExcel);
        pbfCollection.updateById(pbf.getId(), pbf);

        logShuffle(sheetName, pbf);
    }

    public List<Unit> drawUnitsFromForBattle(String pbfId, String playerId, int numberOfDraws) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        playerhand.getBattlehand().clear();
        List<Unit> unitsInHand = playerhand.getItems().stream()
                .filter(p -> SheetName.UNITS.contains(p.getSheetName()))
                .map(p -> (Unit) p)
                .collect(Collectors.toList());

        if (unitsInHand.isEmpty()) {
            return unitsInHand;
        }

        //Check if already units in battle
        /*long inBattle = unitsInHand.stream()
                .filter(Unit::isInBattle)
                .count();
        if (inBattle > 0L) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .build());
        }*/

        if (unitsInHand.size() <= numberOfDraws) {
            //username has drawn X units from his battlehand
            playerhand.setBattlehand(unitsInHand);
            pbfCollection.updateById(pbfId, pbf);
            createCommonPublicLog("has drawn " + unitsInHand.size() + " units his battlehand", pbfId, playerId);
            return unitsInHand;
        }

        Collections.shuffle(unitsInHand);
        List<Unit> drawnUnitsList = unitsInHand.stream().limit(numberOfDraws)
                //.peek(item -> createCommonPrivateLog("has drawn " + item.getName() + " for battle from your battlehand", pbfId, playerId))
                .collect(Collectors.toList());

        playerhand.setBattlehand(drawnUnitsList);
        pbfCollection.updateById(pbfId, pbf);
        createCommonPublicLog("has drawn " + drawnUnitsList.size() + " units from his battlehand", pbfId, playerId);
        return drawnUnitsList;
    }

    //Not sure yet if I want this since they can just make a new draw
    //TODO Reveal instead
    public void discardUnitsFromBattlehand(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        playerhand.getBattlehand().clear();
        pbfCollection.updateById(pbf.getId(), pbf);
        gameLogAction.createCommonPrivatePublicLog("has cleared their battlehand", pbfId, playerId);
    }

    public List<Unit> drawBarbarians(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!playerhand.getBarbarians().isEmpty()) {
            log.warn("Cannot draw more barbarians until they are discarded");
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(Entity.json(new MessageDTO("Cannot draw more barbarians until they are discarded"))).build());
        }
        //Check first and reshuffle
        //TODO If either barbarian units are empty, then no barbs are drawn. Perhaps, draw something else
        if (!pbf.getItems().stream().anyMatch(p -> p.getSheetName() == SheetName.INFANTRY)) {
            reshuffleItems(SheetName.INFANTRY, pbf);
            drawBarbarians(pbfId, playerId);
        }

        if (!pbf.getItems().stream().anyMatch(p -> p.getSheetName() == SheetName.MOUNTED)) {
            reshuffleItems(SheetName.MOUNTED, pbf);
            drawBarbarians(pbfId, playerId);
        }

        if (!pbf.getItems().stream().anyMatch(p -> p.getSheetName() == SheetName.ARTILLERY)) {
            reshuffleItems(SheetName.ARTILLERY, pbf);
            drawBarbarians(pbfId, playerId);
        }

        Iterator<Item> iterator = pbf.getItems().iterator();
        boolean foundInfantry = false, foundArtillery = false, foundMounted = false;
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (!foundInfantry && item.getSheetName() == SheetName.INFANTRY) {
                foundInfantry = true;
                addBarbarian(playerId, playerhand, iterator, item);
            } else if (!foundArtillery && item.getSheetName() == SheetName.ARTILLERY) {
                foundArtillery = true;
                addBarbarian(playerId, playerhand, iterator, item);
            } else if (!foundMounted && item.getSheetName() == SheetName.MOUNTED) {
                foundMounted = true;
                addBarbarian(playerId, playerhand, iterator, item);
            }
        }

        if (playerhand.getBarbarians().size() != 3) {
            log.error("Couldn't get one barbarian of each type, but instead list is " + playerhand.getBarbarians());
            throw new WebApplicationException(Response.status(Response.Status.EXPECTATION_FAILED)
                    .entity(Entity.json(new MessageDTO("Couldn't get one barbarian of each type"))).build());
        }

        gameLogAction.createCommonPrivatePublicLog("drew 3 barbarian units", pbfId, playerId);
        pbfCollection.updateById(pbf.getId(), pbf);
        return playerhand.getBarbarians();
    }

    public void discardBarbarians(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (playerhand.getBarbarians().isEmpty()) {
            return;
        }

        pbf.getDiscardedItems().addAll(playerhand.getBarbarians());
        revealAndDiscardUnits(" as barbarians", playerhand.getBarbarians(), pbfId, playerId);
        pbfCollection.updateById(pbf.getId(), pbf);
    }

    public void revealAndDiscardBattlehand(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (playerhand.getBattlehand().isEmpty()) {
            log.warn("Tried to reveal playerhand, but was empty");
            return;
        }

        revealAndDiscardUnits(" from their battlehand", playerhand.getBattlehand(), pbfId, playerId);
        pbfCollection.updateById(pbfId, pbf);
    }

    private void revealAndDiscardUnits(String message, List<Unit> units, String pbfId, String playerId) {
        //Clear just in case
        sb.setLength(0);
        units.forEach(revealUnitConsumer);

        //Remove the last append ', '
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        createCommonPublicLog(" reveals " + sb.toString() + message, pbfId, playerId);
        units.clear();
    }

    /**
     * Sets playerhands units to isBattle = false
     *
     * @param pbfId
     * @param playerId
     */
    public void endBattle(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);

        playerhand.getItems().stream()
                .filter(item -> SheetName.UNITS.contains(item.getSheetName()))
                .forEach(item -> {
                    Unit unit = (Unit) item;
                    unit.setInBattle(false);
                });

        pbfCollection.updateById(pbfId, pbf);
    }

    /**
     * draws a random item from playerhand and gives to another player
     *  @param pbfId          - The pbf id
     * @param sheetName      - the item to be automatically drawn from playerhand and given to another player
     * @param targetPlayerId - The targeted player which will recieve the item
     * @param playerId       - The logged in player that we will take the item from
     */
    public Item drawRandomItemAndGiveToPlayer(String pbfId, SheetName sheetName, String targetPlayerId, String playerId) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(sheetName);
        Preconditions.checkNotNull(targetPlayerId);
        Preconditions.checkNotNull(playerId);

        PBF pbf = findPBFById(pbfId);
        Playerhand playerFrom = getPlayerhandByPlayerId(playerId, pbf);
        Playerhand playerTo = getPlayerhandByPlayerId(targetPlayerId, pbf);

        //Locate the item from player
        List<Item> itemToShuffle = playerFrom.getItems().stream()
                .filter(it -> it.getSheetName() == sheetName)
                .collect(Collectors.toList());
        if (itemToShuffle.isEmpty()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(new MessageDTO("You have no " + sheetName.getName() + " to draw"))
                            .build()
            );
        }

        if(!(itemToShuffle.get(0) instanceof Tradable)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_ACCEPTABLE)
                            .entity(new MessageDTO(sheetName.getName() + " is not lootable"))
                            .build()
            );
        }

        Collections.shuffle(itemToShuffle);
        Item itemToGive = itemToShuffle.get(0);
        log.debug(playerFrom.getUsername() + " gives " + itemToGive + " to " + playerTo.getUsername());
        boolean removed = playerFrom.getItems().remove(itemToGive);
        if (removed) {
            //Give to the other player
            playerTo.getItems().add(itemToGive);
            createCommonPrivateLog(" gives " + itemToGive.revealAll() + " to " + playerTo.getUsername(), pbfId, playerFrom.getPlayerId());
            createCommonPrivateLog(" receives " + itemToGive.revealAll() + " from " + playerFrom.getUsername(), pbfId, playerTo.getPlayerId());

            createCommonPublicLog(" gives " + itemToGive.revealPublic() + " to " + playerTo.getUsername(), pbfId, playerFrom.getPlayerId());
            createCommonPublicLog(" receives " + itemToGive.revealPublic() + " from " + playerFrom.getUsername(), pbfId, playerTo.getPlayerId());

            pbfCollection.updateById(pbf.getId(), pbf);
            return itemToGive;
        } else {
            throw cannotFindItem();
        }

    }

    private void addBarbarian(String playerId, Playerhand playerhand, Iterator<Item> iterator, Item item) {
        playerhand.getBarbarians().add((Unit) item);
        iterator.remove();
        item.setOwnerId(playerId);
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
        playerhand.getItems().add(item);
    }

    private static Draw<Item> createDraw(String pbfId, String playerId, Item item) {
        Draw<Item> draw = new Draw<>(pbfId, playerId);
        item.setHidden(true);
        draw.setItem(item);
        return draw;
    }
}