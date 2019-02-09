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
import no.asgari.civilization.server.exception.ForbiddenException;
import no.asgari.civilization.server.exception.NotFoundException;
import no.asgari.civilization.server.model.*;
import no.asgari.civilization.server.repository.ChatRepository;
import no.asgari.civilization.server.repository.GameLogRepository;
import no.asgari.civilization.server.repository.PBFRepository;
import no.asgari.civilization.server.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public abstract class BaseAction {
    @Autowired
    protected GameLogAction logAction;

    @Autowired
    protected GameLogRepository gameLogRepository;

    @Autowired
    protected PBFRepository pbfRepository;

    @Autowired
    protected ChatRepository chatRepository;

    @Autowired
    protected PlayerRepository playerRepository;

    public static NotFoundException cannotFindItem() {
        throw new NotFoundException();
    }

    static NotFoundException cannotFindPlayer() {
        throw new NotFoundException();
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

    protected GameLog createLog(String pbfId, GameLog.LogType logType, String playerId) {
        return logAction.createGameLog(pbfId, logType, playerId);
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
        return pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
    }

    /**
     * Checks whether is the players turn. If not FORBIDDEN exception is thrown
     *
     * @param pbfId
     * @param playerId
     */
    void checkYourTurn(String pbfId, String playerId) {
        PBF pbf = pbfRepository.findById(pbfId).orElseThrow(NotFoundException::new);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        checkYourTurn(playerhand);
    }

    void checkYourTurn(Playerhand playerhand) {
        if (!playerhand.isYourTurn()) {
            throw new ForbiddenException();
        }
    }

    public Playerhand getPlayerhandByPlayerId(String playerId, PBF pbf) {
        return pbf.getPlayers()
                .stream().filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindPlayer);
    }

}
