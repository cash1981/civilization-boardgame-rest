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
import no.asgari.civilization.server.dto.MessageDTO;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.SocialPolicy;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.List;

@Log4j
public abstract class BaseAction {
    protected final GameLogAction logAction;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    protected BaseAction(DB db) {
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.logAction = new GameLogAction(db);
    }

    /**
     * Creates public and private logs of draws *
     */
    protected GameLog createLog(Draw<? extends Spreadsheet> draw, GameLog.LogType logType) {
        return logAction.createGameLog(draw, logType);
    }

    protected GameLog createLog(Tech chosenTech, String pbfId, GameLog.LogType logType) {
        return logAction.createGameLog(chosenTech, pbfId, logType);
    }

    protected GameLog createLog(SocialPolicy socialPolicy, String pbfId, GameLog.LogType logType) {
        return logAction.createGameLog(socialPolicy, pbfId, logType);
    }

    protected GameLog createLog(Item item, String pbfId, GameLog.LogType logType, String playerId) {
        return logAction.createGameLog(item, pbfId, logType, playerId);
    }

    protected GameLog createLog(Draw draw, String pbfId, String username, boolean vote) {
        return logAction.createGameLog(draw, pbfId, username, vote);
    }

    protected GameLog createCommonPrivateLog(String privateMessage, String pbfId, String playerId) {
        return logAction.createCommonPrivateLog(privateMessage, pbfId, playerId);
    }

    protected GameLog createCommonPublicLog(String message, String pbfId, String playerId) {
        return logAction.createCommonPublicLog(message, pbfId, playerId);
    }

    protected void createInfoLog(String pbfId, String message) {
        Preconditions.checkNotNull(message);
        GameLog log = new GameLog();
        log.setPublicLog("System: " + message);
        log.setUsername("System");
        log.setPbfId(pbfId);
        logAction.save(log);
    }

    public PBF findPBFById(String pbfId) {
        try {
            return pbfCollection.findOneById(pbfId);
        } catch (Exception ex) {
            log.error("Couldn't find pbf");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity(Entity.json(new MessageDTO("Could not find game by id")))
                    .build();
            throw new WebApplicationException(badReq);
        }
    }

    /**
     * Checks whether is the players turn. If not FORBIDDEN exception is thrown
     *
     * @param pbfId
     * @param playerId
     * @throws WebApplicationException(Response) - Throws Response.FORBIDDEN if not your turn
     */
    //TODO Perhaps its best to have this in a filter, but its not always intended to be run
    void checkYourTurn(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        checkYourTurn(playerhand);
    }

    void checkYourTurn(Playerhand playerhand) {
        if (!playerhand.isYourTurn()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity(Entity.json(new MessageDTO("Its not your turn!")))
                    .build());
        }
    }

    Playerhand getPlayerhandByPlayerId(String playerId, PBF pbf) {
        return pbf.getPlayers()
                .stream().filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindPlayer);
    }

    static WebApplicationException cannotFindItem() {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(Entity.json(new MessageDTO("Could not find item")))
                .build());
    }

    static WebApplicationException cannotFindPlayer() {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(Entity.json(new MessageDTO("Could not find player")))
                .build());
    }

}
