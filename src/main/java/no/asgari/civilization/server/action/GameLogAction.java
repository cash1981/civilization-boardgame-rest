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
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.exception.NotFoundException;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Action class responsible for logging private and public logs
 */
@Slf4j
@Component
public class GameLogAction extends BaseAction {

    String save(@NotNull @Valid GameLog gameLog) {
        Preconditions.checkNotNull(gameLog);

        GameLog insert = gameLogRepository.save(gameLog);
        return insert.getId();
    }

    public GameLog createGameLog(Draw draw, GameLog.LogType logType) {
        GameLog pl = new GameLog();
        pl.setDraw(draw);
        pl.setPbfId(draw.getPbfId());
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.createAndSetLog(logType, draw.getItem() != null ? draw.getItem().getItemNumber() : 0);
        pl.setId(save(pl));
        return pl;
    }

    public GameLog createGameLog(Item item, String pbfId, GameLog.LogType logType) {
        return createGameLog(item, pbfId, logType, item.getOwnerId());
    }

    public GameLog createGameLog(Item item, String pbfId, GameLog.LogType logType, String playerId) {
        GameLog pl = new GameLog();
        Draw<Item> draw = new Draw<>(pbfId, item.getOwnerId());
        draw.setItem(item);
        pl.setDraw(draw);
        pl.setPbfId(pbfId);
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(playerId));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(playerId));
        }
        pl.createAndSetLog(logType, item.getItemNumber());
        pl.setId(save(pl));
        return pl;
    }

    public GameLog createGameLog(Draw draw, String pbfId, String username, boolean vote) {
        GameLog pl = new GameLog();
        pl.setPbfId(pbfId);
        pl.setUsername(username);
        pl.setPublicLog(username + " has voted " + (vote ? "yes" : "no") + " to undo "
                + draw.getItem().revealPublic() + " with item number " + draw.getItem().getItemNumber());
        pl.setId(save(pl));
        return pl;
    }

    /**
     * Common messages like, user joined game, user withdrew game etc
     */
    public GameLog createCommonPublicLog(String publicMessage, String pbfId, String playerId) {
        GameLog pl = createCommonGameLog(pbfId, playerId);
        pl.setPublicLog(pl.getUsername() + " " + publicMessage);
        pl.setPrivateLog("");
        pl.setId(save(pl));
        return pl;
    }

    public GameLog createCommonPrivateLog(String privateMessage, String pbfId, String playerId) {
        GameLog pl = createCommonGameLog(pbfId, playerId);
        pl.setPrivateLog(pl.getUsername() + " " + privateMessage);
        pl.setPublicLog("");
        pl.setId(save(pl));
        return pl;
    }

    public GameLog createCommonPrivatePublicLog(String message, String pbfId, String playerId) {
        GameLog pl = createCommonGameLog(pbfId, playerId);
        pl.setPrivateLog(pl.getUsername() + " " + message);
        pl.setPublicLog(pl.getUsername() + " " + message);
        pl.setId(save(pl));
        return pl;
    }

    private GameLog createCommonGameLog(String pbfId, String playerId) {
        GameLog pl = new GameLog();
        pl.setPbfId(pbfId);
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(playerId));
        } catch (Exception e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(playerId));
        }
        return pl;
    }

    public GameLog findGameLogById(String id) {
        return gameLogRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public GameLog updateGameLogById(GameLog gameLog) {
        return gameLogRepository.save(gameLog);
    }

    private String getUsernameFromPlayerId(String playerId) {
        return playerRepository.findById(playerId).orElseThrow(NotFoundException::new).getUsername();
    }

    public List<GameLog> getGameLogs(String pbfId) {
        return gameLogRepository.findAllByPbfId(pbfId);
    }

    public List<GameLog> getGameLogsBelongingToPlayer(String pbfId, String username) {
        return gameLogRepository.findAllByPbfIdAndUsername(pbfId, username);
    }

    public void createTradeGameLog(Item item, String pbfId, GameLog.LogType logType, String username) {
        GameLog gl = new GameLog();
        Draw<Item> draw = new Draw<>(pbfId, item.getOwnerId());
        draw.setItem(item);
        gl.setDraw(draw);
        gl.setPbfId(pbfId);
        try {
            gl.setUsername(CivSingleton.instance().playerCache().get(item.getOwnerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            gl.setUsername(getUsernameFromPlayerId(item.getOwnerId()));
        }
        gl.createAndSetLog(logType, item.getItemNumber());
        gl.setId(save(gl));

        GameLog gl2 = new GameLog();
        gl2.setPbfId(pbfId);
        gl2.setUsername(username);
        gl2.setPrivateLog(gl.getUsername() + " has received - " + draw.getItem().revealAll());
        gl2.setPublicLog("");
        gl2.setId(save(gl2));
    }

    public GameLog createGameLog(String pbfId, GameLog.LogType logType, String playerId) {
        GameLog pl = new GameLog();
        pl.setPbfId(pbfId);
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(playerId));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(playerId));
        }
        pl.createAndSetLog(logType, 0);
        pl.setId(save(pl));
        return pl;
    }

    public boolean updateGameLog(String pbfId, String oldUsername, String newUsername) {
        List<GameLog> gameLogs = gameLogRepository.findAllByPbfIdAndUsername(pbfId, oldUsername);
        for (GameLog gl : gameLogs) {
            gl.setUsername(newUsername);
            gameLogRepository.save(gl);
        }
        return !gameLogs.isEmpty();
    }

    public void createUndoLog(String pbfId, String message, Item item) {
        Preconditions.checkNotNull(message);
        GameLog log = new GameLog();
        log.setPublicLog("System: " + message + ". Item number #" + item.getItemNumber());
        log.setPrivateLog("System: " + message + ". Item number #" + item.getItemNumber());
        log.setUsername("System");
        log.setPbfId(pbfId);
        save(log);
    }
}
