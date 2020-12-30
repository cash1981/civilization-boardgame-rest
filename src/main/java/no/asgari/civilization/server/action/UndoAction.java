/*
 * Copyright (c) 2015-2021 Shervin Asgari
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
import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Undo;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Log4j
public class UndoAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;

    public UndoAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    private boolean putDrawnItemBackInPBF(PBF pbf, String playerId, Item item) {
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        if (item instanceof Tech) {
            //Remove from tech
            if (playerhand.getTechsChosen().remove(item)) {
                logAction.createUndoLog(pbf.getId(), "has removed " + item.getName() + " from " + playerhand.getUsername(), item);
                log.debug("Successfully undoed tech");
            } else if (pbf.getDiscardedItems().remove(item)) {
                playerhand.getTechsChosen().add((Tech) item);
                logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
            } else {
                log.error("Didn't find tech to remove from playerhand: " + item);
                return false;
            }
        } else {
            if (playerhand.getItems().remove(item)) {
                item.setHidden(true);
                logAction.createUndoLog(pbf.getId(), "has removed " + item.getName() + " from " + playerhand.getUsername() + " and put back in the deck. Deck is reshuffled", item);
                pbf.getItems().add(item);
                shufflePBFTwice(pbf);
                log.debug("Successfully undoed item");
            } else if (pbf.getDiscardedItems().remove(item)) {
                item.setHidden(true);
                playerhand.getItems().add(item);
                logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
            } else if (pbf.getItems().remove(item)) {
                //In rare cases the item is put back to the player
                item.setHidden(true);
                playerhand.getItems().add(item);
                logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
            } else {
                log.error("Didn't find item to remove from playerhand: " + item);
                return false;
            }
        }

        pbfCollection.updateById(pbf.getId(), pbf);
        return true;
    }

    private boolean putDrawnItemBackInPBF(PBF pbf, Draw draw) {
        Preconditions.checkNotNull(pbf);
        Preconditions.checkNotNull(draw);
        Preconditions.checkNotNull(draw.getItem());

        Item item = draw.getItem();
        return putDrawnItemBackInPBF(pbf, item.getOwnerId(), draw);
    }

    private boolean putDrawnItemBackInPBF(PBF pbf, String playerId, Draw draw) {
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        Item item = draw.getItem();
        if (item instanceof Tech) {
            //Remove from tech
            if (playerhand.getTechsChosen().remove(item)) {
                logAction.createUndoLog(pbf.getId(), "has removed " + item.getName() + " from " + playerhand.getUsername(), item);
                log.debug("Successfully undoed tech");
            } else if (pbf.getDiscardedItems().remove(item)) {
                playerhand.getTechsChosen().add((Tech) item);
                logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
            } else {
                log.error("Didn't find tech to remove from playerhand: " + item);
                return false;
            }
        } else {
            if (!Strings.isNullOrEmpty(draw.getGameLogId())) {
                GameLog gamelog = gameLogCollection.findOneById(draw.getGameLogId());
                if (gamelog.getPrivateLog().contains("discarded")) {
                    if (pbf.getDiscardedItems().remove(item)) {
                        item.setHidden(true);
                        playerhand.getItems().add(item);
                        logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
                    }
                } else if (gamelog.getPrivateLog().contains("drew") && !gamelog.getPrivateLog().contains("barbarian")) {
                    if (playerhand.getItems().remove(item) || pbf.getDiscardedItems().remove(item)) {
                        item.setHidden(true);
                        logAction.createUndoLog(pbf.getId(), "has removed " + item.getName() + " from " + playerhand.getUsername() + " and put back in the deck. Deck is reshuffled", item);
                        pbf.getItems().add(item);
                        shufflePBFTwice(pbf);
                    }
                } else if (gamelog.getPrivateLog().contains("drew") && gamelog.getPrivateLog().contains("barbarian")) {
                    pbf.getItems().addAll(playerhand.getBarbarians());
                    playerhand.getBarbarians().clear();
                    logAction.createUndoLog(pbf.getId(), "has removed barbarians from " + playerhand.getUsername() + " and put back in the deck. Deck is reshuffled", item);
                    shufflePBFTwice(pbf);
                } else if (pbf.getItems().remove(item)) {
                    //In rare cases the item is put back to the player (Not sure if I need this)
                    item.setHidden(true);
                    playerhand.getItems().add(item);
                    logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
                } else {
                    log.error("Didn't find item to remove from playerhand: " + item);
                    return false;
                }
            } else {
                //Backward compability
                if (playerhand.getItems().remove(item)) {
                    item.setHidden(true);
                    logAction.createUndoLog(pbf.getId(), "has removed " + item.getName() + " from " + playerhand.getUsername() + " and put back in the deck. Deck is reshuffled", item);
                    pbf.getItems().add(item);
                    shufflePBFTwice(pbf);
                    log.debug("Successfully undoed item");
                } else if (pbf.getDiscardedItems().remove(item)) {
                    item.setHidden(true);
                    playerhand.getItems().add(item);
                    logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
                } else if (pbf.getItems().remove(item)) {
                    //In rare cases the item is put back to the player
                    item.setHidden(true);
                    playerhand.getItems().add(item);
                    logAction.createUndoLog(pbf.getId(), "has added back " + item.getName() + " to " + playerhand.getUsername(), item);
                } else {
                    log.error("Didn't find item to remove from playerhand: " + item);
                    return false;
                }
            }
        }

        pbfCollection.updateById(pbf.getId(), pbf);
        return true;
    }

    private void shufflePBFTwice(PBF pbf) {
        Collections.shuffle(pbf.getItems(), new Random(System.nanoTime()));
        Collections.shuffle(pbf.getItems(), new Random(System.nanoTime()));
    }

    /**
     * Will perform vote, and if all votes are successfull, item is put back in the deck
     * <p>
     * A vote must have already been initiated
     *
     * @param gameLog
     * @param playerId
     * @param vote
     * @return
     */
    public GameLog vote(GameLog gameLog, String playerId, boolean vote) {
        Preconditions.checkNotNull(gameLog);
        Preconditions.checkNotNull(gameLog.getDraw());
        Preconditions.checkNotNull(gameLog.getDraw().getUndo());

        PBF pbf = pbfCollection.findOneById(gameLog.getPbfId());
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);

        if (gameLog.getDraw() == null || gameLog.getDraw().getUndo() == null) {
            log.error("This item cannot be undone. Nothing to undo.");
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .build());
        }

        gameLog.getDraw().getUndo().vote(playerId, vote);
        gameLog.getDraw().setGameLogId(gameLog.getId());
        createLog(gameLog.getDraw(), pbf.getId(), playerhand.getUsername(), vote);

        Optional<Boolean> resultOfVotes = gameLog.getDraw().getUndo().getResultOfVotes();
        if (resultOfVotes.isPresent() && resultOfVotes.get()) {
            log.info("Everyone has performed a vote, so we put item back in the deck");
            gameLog.getDraw().getUndo().setDone(true);
            putDrawnItemBackInPBF(pbf, gameLog.getDraw());
        }
        gameLogCollection.updateById(gameLog.getId(), gameLog);
        return gameLog;
    }

    /**
     * Will request for undo and initiate a vote
     *
     * @param logContainingItemToUndo
     * @param playerId
     * @return
     */
    public void initiateUndo(GameLog logContainingItemToUndo, String playerId) {
        Preconditions.checkNotNull(logContainingItemToUndo);
        Preconditions.checkNotNull(logContainingItemToUndo.getDraw());

        Draw<?> draw = logContainingItemToUndo.getDraw();
        if (draw.getUndo() != null) {
            log.error("Cannot initiate a undo. Its already been initiated");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .build());
        }
        PBF pbf = pbfCollection.findOneById(logContainingItemToUndo.getPbfId());
        if (!pbf.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId))) {
            log.error("Couldn't find playerId " + playerId + " in PBF's players");
            throw PlayerAction.cannotFindPlayer();
        }

        draw.setUndo(new Undo(pbf.getNumOfPlayers(), playerId));
        gameLogCollection.updateById(logContainingItemToUndo.getId(), logContainingItemToUndo);

        createLog(draw.getItem(), pbf.getId(), GameLog.LogType.UNDO, playerId);
    }

    public List<GameLog> getAllActiveUndos(String pbfId) {
        List<GameLog> gameLogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId), new BasicDBObject()).toArray();

        return gameLogs.stream()
                .filter(GameLog::hasActiveUndo)
                .collect(Collectors.toList());
    }

    public List<GameLog> getPlayersActiveUndoes(String pbfId, String username) {
        List<GameLog> gamelogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId).is("username", username), new BasicDBObject()).toArray();

        return gamelogs.stream()
                .filter(GameLog::hasActiveUndo)
                .collect(Collectors.toList());
    }

    public List<GameLog> getAllFinishedUndos(String pbfId) {
        List<GameLog> gameLogs = gameLogCollection.find(DBQuery.is("pbfId", pbfId), new BasicDBObject()).toArray();

        return gameLogs.stream()
                .filter(log -> log.getDraw() != null && log.getDraw().getUndo() != null && log.getDraw().getUndo().isDone())
                .collect(Collectors.toList());
    }

    public void playerPutsItemBackInDeck(String pbfId, String playerId, ItemDTO itemdto) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        Optional<SheetName> dtoSheet = SheetName.find(itemdto.getSheetName());
        if (!dtoSheet.isPresent()) {
            log.error("Couldn't find sheetname " + itemdto.getSheetName());
            throw cannotFindItem();
        }

        //Find the item, then putback to deck
        Optional<Item> itemToPutBack = playerhand.getItems().stream()
                .filter(item -> item.getSheetName() == dtoSheet.get() && item.getName().equals(itemdto.getName()))
                .findAny();

        if (!itemToPutBack.isPresent()) {
            throw cannotFindItem();
        }

        Item item = itemToPutBack.get();
        boolean ok = putDrawnItemBackInPBF(pbf, playerId, item);
        if (!ok) {
            throw cannotFindItem();
        }
    }
}
