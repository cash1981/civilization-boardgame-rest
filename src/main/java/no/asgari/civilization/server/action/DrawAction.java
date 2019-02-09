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
import lombok.extern.slf4j.Slf4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.exception.GoneException;
import no.asgari.civilization.server.exception.NotAcceptableException;
import no.asgari.civilization.server.exception.NotFoundException;
import no.asgari.civilization.server.exception.PreConditionFailedException;
import no.asgari.civilization.server.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static no.asgari.civilization.server.SheetName.*;

/**
 * Class that will perform draws and log them.
 * Draws will be saved in the gamelog collection
 */
@Slf4j
@Component
public class DrawAction extends BaseAction {
    private final GameLogAction gameLogAction;

    private final StringBuilder sb = new StringBuilder();
    private final Consumer<Unit> revealUnitConsumer = unit -> sb.append(unit.revealAll()).append(", ");

    public DrawAction(GameLogAction gameLogAction) {
        this.gameLogAction = gameLogAction;
    }

    private static Draw<Item> createDraw(String pbfId, String playerId, Item item) {
        Draw<Item> draw = new Draw<>(pbfId, playerId);
        item.setHidden(true);
        draw.setItem(item);
        return draw;
    }

    public Optional<GameLog> draw(String pbfId, String playerId, SheetName sheetName) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);
        Preconditions.checkNotNull(sheetName);

        checkYourTurn(pbfId, playerId);
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);

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
                    pbfRepository.save(pbf);
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

    private void reshuffleItems(SheetName sheetName, PBF pbf) throws GoneException {
        if (!SheetName.SHUFFLABLE_ITEMS.contains(sheetName)) {
            log.warn("Tried to reshuffle " + sheetName.getName() + " but not a shufflable type");
            throw new IllegalArgumentException();
        }

        List<Item> itemsToPutBackInDeck = pbf.getDiscardedItems().stream()
                .filter(s -> s.getSheetName() == sheetName)
                .collect(toList());

        if (itemsToPutBackInDeck.isEmpty()) {
            log.warn("All items are still in use, cannot make a shuffle. Nothing to draw!");
            throw new GoneException();
        }

        List<Item> itemsToKeep = pbf.getDiscardedItems().stream()
                .filter(s -> s.getSheetName() != sheetName)
                .collect(toList());

        log.debug("Shuffling, and adding items back in the pbf");
        Collections.shuffle(itemsToPutBackInDeck);
        pbf.getItems().addAll(itemsToPutBackInDeck);
        pbf.setDiscardedItems(itemsToKeep);

        pbfRepository.save(pbf);
        logShuffle(sheetName, pbf);
    }

    public List<Unit> drawUnitsFromBattlehandForBattle(String pbfId, String playerId, int numberOfDraws) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        playerhand.getBattlehand().clear();
        List<Unit> unitsInHand = playerhand.getItems().stream()
                .filter(p -> SheetName.UNITS.contains(p.getSheetName()))
                .map(p -> (Unit) p)
                .collect(toList());

        if (unitsInHand.isEmpty()) {
            return unitsInHand;
        }

        if (unitsInHand.size() <= numberOfDraws) {
            //username has drawn X units from his battlehand
            playerhand.setBattlehand(unitsInHand);
            pbfRepository.save(pbf);
            createCommonPublicLog("has drawn " + unitsInHand.size() + " units his battlehand", pbfId, playerId);
            return unitsInHand;
        }

        Collections.shuffle(unitsInHand);
        List<Unit> drawnUnitsList = unitsInHand.stream().limit(numberOfDraws).collect(toList());

        playerhand.setBattlehand(drawnUnitsList);
        pbfRepository.save(pbf);
        createCommonPublicLog("has drawn " + drawnUnitsList.size() + " units from his battlehand", pbfId, playerId);
        return drawnUnitsList;
    }

    /**
     * Draws up to three barbarians.
     * If it cannot get a type, it will try to draw another
     *
     * @param pbfId
     * @param playerId
     * @return
     */
    public List<Unit> drawBarbarians(String pbfId, String playerId) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (!playerhand.getBarbarians().isEmpty()) {
            log.warn("Cannot draw more barbarians until they are discarded");
            throw new PreConditionFailedException();
        }

        drawBarbarianInfantry(pbf, playerhand);
        drawBarbarianArtillery(pbf, playerhand);
        drawBarbarianMounted(pbf, playerhand);

        pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        playerhand = getPlayerhandByPlayerId(playerId, pbf);
        gameLogAction.createCommonPrivatePublicLog("has drawn " + playerhand.getBarbarians().size() + " barbarian units", pbfId, playerId);
        return playerhand.getBarbarians();
    }

    private void drawBarbarianInfantry(PBF pbf, Playerhand playerhand) {
        boolean anyInfantry = pbf.getItems().stream().anyMatch(p -> p.getSheetName() == INFANTRY);
        if (!anyInfantry) {
            try {
                reshuffleItems(INFANTRY, pbf);
                drawBarbarianInfantry(pbf, playerhand);
            } catch (GoneException e) {
                StringBuilder stringBuilder = new StringBuilder();
                if (tryToFindUnit(pbf, playerhand, stringBuilder, ARTILLERY, INFANTRY) || tryToFindUnit(pbf, playerhand, stringBuilder, MOUNTED, INFANTRY)) {
                    gameLogAction.createCommonPrivatePublicLog(stringBuilder.toString(), pbf.getId(), playerhand.getPlayerId());
                } else {
                    throw new GoneException();
                }

            }
        } else {
            Iterator<Item> iterator = pbf.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getSheetName() == SheetName.INFANTRY) {
                    addBarbarian(pbf, playerhand, iterator, item);
                    return;
                }
            }
        }
    }

    private void drawBarbarianMounted(PBF pbf, Playerhand playerhand) {
        boolean any = pbf.getItems().stream().anyMatch(p -> p.getSheetName() == MOUNTED);

        if (!any) {
            try {
                reshuffleItems(MOUNTED, pbf);
                drawBarbarianMounted(pbf, playerhand);
            } catch (GoneException e) {
                StringBuilder stringBuilder = new StringBuilder();
                if (tryToFindUnit(pbf, playerhand, stringBuilder, ARTILLERY, MOUNTED) || tryToFindUnit(pbf, playerhand, stringBuilder, INFANTRY, MOUNTED)) {
                    gameLogAction.createCommonPrivatePublicLog(stringBuilder.toString(), pbf.getId(), playerhand.getPlayerId());
                } else {
                    throw new GoneException();
                }
            }
        } else {
            Iterator<Item> iterator = pbf.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getSheetName() == SheetName.MOUNTED) {
                    addBarbarian(pbf, playerhand, iterator, item);
                    return;
                }
            }
        }

    }

    private void drawBarbarianArtillery(PBF pbf, Playerhand playerhand) {
        boolean any = pbf.getItems().stream().anyMatch(p -> p.getSheetName() == ARTILLERY);

        if (!any) {
            try {
                reshuffleItems(ARTILLERY, pbf);
                drawBarbarianArtillery(pbf, playerhand);
            } catch (GoneException e) {
                StringBuilder stringBuilder = new StringBuilder();
                if (tryToFindUnit(pbf, playerhand, stringBuilder, MOUNTED, ARTILLERY) || tryToFindUnit(pbf, playerhand, stringBuilder, INFANTRY, ARTILLERY)) {
                    gameLogAction.createCommonPrivatePublicLog(stringBuilder.toString(), pbf.getId(), playerhand.getPlayerId());
                } else {
                    throw new GoneException();
                }
            }
        } else {
            Iterator<Item> iterator = pbf.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getSheetName() == SheetName.ARTILLERY) {
                    addBarbarian(pbf, playerhand, iterator, item);
                    return;
                }
            }
        }
    }

    private boolean tryToFindUnit(PBF pbf, Playerhand playerhand, StringBuilder stringBuilder, SheetName sheetToDraw, SheetName originalSheet) {
        Optional<Item> anyUnit = pbf.getItems().stream().filter(p -> p.getSheetName() == sheetToDraw).findAny();
        if (anyUnit.isPresent()) {
            Iterator<Item> iterator = pbf.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getSheetName() == sheetToDraw) {
                    addBarbarian(pbf, playerhand, iterator, item);
                    stringBuilder.append(" tried to draw ")
                            .append(originalSheet.getName())
                            .append(" barbarian unit. However there are no more in the deck. Will instead draw ")
                            .append(sheetToDraw.getName())
                            .append(" unit instead!");
                    return true;
                }
            }
        }
        return false;
    }

    public void discardBarbarians(String pbfId, String playerId) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (playerhand.getBarbarians().isEmpty()) {
            return;
        }
        playerhand.getBarbarians().forEach(unit -> unit.setOwnerId(null));
        pbf.getDiscardedItems().addAll(playerhand.getBarbarians());
        revealAndDiscardUnits(" as barbarians", playerhand.getBarbarians(), pbfId, playerId);
        pbfRepository.save(pbf);
    }

    public void revealAndDiscardBattlehand(String pbfId, String playerId) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (playerhand.getBattlehand().isEmpty()) {
            log.warn("Tried to reveal playerhand, but was empty");
            return;
        }

        revealAndDiscardUnits(" from their battlehand", playerhand.getBattlehand(), pbfId, playerId);
        pbfRepository.save(pbf);
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
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);

        playerhand.getItems().stream()
                .filter(item -> SheetName.UNITS.contains(item.getSheetName()))
                .forEach(item -> {
                    Unit unit = (Unit) item;
                    unit.setInBattle(false);
                });

        pbfRepository.save(pbf);
    }

    /**
     * draws a random item from playerhand and gives to another player
     *
     * @param pbfId          - The pbf id
     * @param sheetNames     - the item to be automatically drawn from playerhand and given to another player
     * @param targetPlayerId - The targeted player which will recieve the item
     * @param playerId       - The logged in player that we will take the item from
     */
    public Item loot(String pbfId, EnumSet<SheetName> sheetNames, String targetPlayerId, String playerId) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(sheetNames);
        Preconditions.checkNotNull(targetPlayerId);
        Preconditions.checkNotNull(playerId);

        PBF pbf = findPBFById(pbfId);
        Playerhand playerFrom = getPlayerhandByPlayerId(playerId, pbf);
        Playerhand playerTo = getPlayerhandByPlayerId(targetPlayerId, pbf);

        //Locate the item from player
        List<Item> itemToShuffle = playerFrom.getItems().stream()
                .filter(it -> sheetNames.contains(it.getSheetName()))
                .collect(toList());
        if (itemToShuffle.isEmpty()) {
            throw new NotFoundException();
        }

        if (!(itemToShuffle.get(0) instanceof Tradable)) {
            throw new NotAcceptableException();
        }

        Collections.shuffle(itemToShuffle);
        Item itemToGive = itemToShuffle.get(0);
        log.debug(playerFrom.getUsername() + " gives " + itemToGive + " to " + playerTo.getUsername());
        boolean removed = playerFrom.getItems().remove(itemToGive);
        if (removed) {
            //Give to the other player
            playerTo.getItems().add(itemToGive);
            createCommonPrivateLog(" is randomly looted " + itemToGive.revealAll() + " and gives to " + playerTo.getUsername(), pbfId, playerFrom.getPlayerId());
            createCommonPrivateLog(" receives as loot " + itemToGive.revealAll() + " from " + playerFrom.getUsername(), pbfId, playerTo.getPlayerId());

            createCommonPublicLog(" is randomly looted " + itemToGive.revealPublic() + " and gives to " + playerTo.getUsername(), pbfId, playerFrom.getPlayerId());
            createCommonPublicLog(" receives as loot " + itemToGive.revealPublic() + " from " + playerFrom.getUsername(), pbfId, playerTo.getPlayerId());

            pbfRepository.save(pbf);
            return itemToGive;
        } else {
            throw cannotFindItem();
        }

    }

    private void addBarbarian(PBF pbf, Playerhand playerhand, Iterator<Item> iterator, Item item) {
        playerhand.getBarbarians().add((Unit) item);
        iterator.remove();
        item.setOwnerId(playerhand.getPlayerId());
        pbfRepository.save(pbf);
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
}