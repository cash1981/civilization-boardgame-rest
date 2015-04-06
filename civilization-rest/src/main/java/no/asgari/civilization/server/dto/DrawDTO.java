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

package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.Undo;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDateTime;

@NoArgsConstructor
@JsonRootName(value = "draw")
@Data
public class DrawDTO {

    public DrawDTO(Draw draw) {
        if(draw != null) {
            this.playerId = draw.getPlayerId();
            this.pbfId = draw.getPbfId();
            this.undo = draw.getUndo();
            this.created = draw.getCreated();
        }
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created;

    /**
     * The user that made the draw. Its always a player that initiates a draw, so this cannot be blank.
     */
    @NotBlank
    private String playerId;
    /**
     * A draw must always belong to a game. The pbf game id
     */
    @NotBlank
    private String pbfId;

    /**
     * If null, then no undo has been performed
     */
    private Undo undo = null;

    /**
     * Returns true if undo has been requested
     */
    @JsonIgnore
    private boolean isUndoInitiated() {
        return undo != null;
    }

}
