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

package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This class will be used to store all the actions performed in the game.
 * It can be creating game, joining game, drawing items
 * <p>
 * All private logs/draws can be requested undo
 * <p>
 * Private view:
 * 14.04.2014 - 14:24 - cash1981 drew Civ Japan - <hidden button> - <undo button>
 * 14.04.2014 - 14:25 - cash1981 drew Civ Greece - <hidden button> - <undo button>
 * <p>
 * Public view:
 * <14.04.2014 - 14:25 - cash1981 drew Infantry
 * <14.04.2014 - 14:25 - cash1981 drew Mounted
 * <14.04.2014 - 14:25 - cash1981 drew Artillery
 */
@JsonRootName("gameLog")
@NoArgsConstructor
@ToString(of = {"privateLog", "publicLog"})
@Data
public class GameLog {
    public static final String COL_NAME = "gamelog";
    private static final String DELIM = " - ";

    public enum LogType {
        TRADE_BETWEEN_PLAYERS, BATTLE, ITEM, TECH, REMOVED_TECH, SHUFFLE, DISCARD, WITHDRAW, JOIN, REVEAL,
        UNDO, SOCIAL_POLICY, REMOVED_SOCIAL_POLICY, VOTE, SETUP, SOT, TRADE , CM, MOVEMENT, RESEARCH;
    }

    @Id
    @ObjectId
    private String id;

    private String privateLog;

    private String publicLog;

    /**
     * Each log belongs to a pbf
     */
    @NotEmpty
    private String pbfId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @NotEmpty
    private String username;

    /**
     * If log is from a draw
     */
    private Draw draw;

    @JsonIgnore
    public long getCreatedInMillis() {
        return created.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @JsonIgnore
    public String uniqueItemNumber(int itemNumber) {
        int uniqueNrThreeLetters = Integer.valueOf(("" + Math.abs(username.hashCode())).substring(0, 3));
        return ". Item number #" + (uniqueNrThreeLetters + itemNumber);
    }

    @JsonIgnore
    public void createAndSetLog(LogType logType, int itemNumber) {
        final String ITEM_NUMBER = ". Item number #" + itemNumber;
        final String UNIQUE_ITEM_NUMBER = uniqueItemNumber(itemNumber);
        switch (logType) {
            case ITEM:
                privateLog = username + " drew " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                publicLog = username + " drew " + DELIM + draw.getItem().revealPublic() + ITEM_NUMBER;
                break;
            case BATTLE:
                privateLog = username + " plays " + DELIM + draw.getItem().revealAll();
                publicLog = username + " reveals " + DELIM + draw.getItem().revealPublic();
                break;
            case TRADE_BETWEEN_PLAYERS:
                privateLog = username + " has received " + DELIM + draw.getItem().revealAll();
                publicLog = username + " has received " + DELIM + draw.getItem().revealPublic();
                break;
            case SOCIAL_POLICY:
                privateLog = username + " has chosen " + DELIM + draw.getItem().revealAll() + UNIQUE_ITEM_NUMBER;
                publicLog = username + " has chosen a hidden social policy" + UNIQUE_ITEM_NUMBER;
                break;
            case TECH:
                privateLog = username + " has researched " + DELIM + draw.getItem().revealAll() + UNIQUE_ITEM_NUMBER;
                publicLog = username + " has researched a hidden technology" + UNIQUE_ITEM_NUMBER;
                break;
            case REMOVED_TECH:
                privateLog = username + " has removed " + DELIM + draw.getItem().revealAll() + UNIQUE_ITEM_NUMBER;
                publicLog = username + " has removed a hidden technology" + UNIQUE_ITEM_NUMBER;
                break;
            case DISCARD:
                privateLog = username + " has discarded " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                publicLog = username + " has discarded " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                break;
            case REVEAL:
                if(draw.getItem() instanceof Tech) {
                    privateLog = username + " has revealed " + DELIM + draw.getItem().revealAll() + UNIQUE_ITEM_NUMBER;
                    publicLog = username + " has revealed " + DELIM + draw.getItem().revealAll() + UNIQUE_ITEM_NUMBER;
                } else {
                    privateLog = username + " has revealed " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                    publicLog = username + " has revealed " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                }
                break;
            case UNDO:
                privateLog = username + " has requested undo of " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                publicLog = username + " has requested undo of " + DELIM + draw.getItem().revealAll() + ITEM_NUMBER;
                break;
            case SOT:
                privateLog = username + " has updated start of turn phase";
                publicLog = username + " has updated start of turn phase";
                break;
            case SETUP:
                privateLog = username + " has updated setup phase";
                publicLog = username + " has updated setup phase";
                break;
            case TRADE:
                privateLog = username + " has updated trade phase";
                publicLog = username + " has updated trade phase";
                break;
            case CM:
                privateLog = username + " has updated city management phase";
                publicLog = username + " has updated city management phase";
                break;
            case MOVEMENT:
                privateLog = username + " has updated movement phase";
                publicLog = username + " has updated movement phase";
                break;
            case RESEARCH:
                privateLog = username + " has updated research phase";
                publicLog = username + " has updated research phase";
                break;
        }
    }

    @JsonIgnore
    public boolean hasUndo() {
        return draw != null && draw.getUndo() != null;
    }

    @JsonIgnore
    public boolean hasActiveUndo() {
        return draw != null && draw.getUndo() != null && !draw.getUndo().isDone();
    }
}
