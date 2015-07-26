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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.model.Chat;

@NoArgsConstructor
@JsonRootName("chat")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ChatDTO {
    private String id;
    private String pbfId;
    private String username;
    private String message;
    private String color;
    private long created;

    public ChatDTO(String id, String pbfId, String username, String message, String color, long createdInMillis) {
        this.id = id;
        this.pbfId = pbfId;
        this.username = username;
        this.message = message;
        this.created = createdInMillis;
        this.color = color;
    }

    public ChatDTO(Chat chat) {
        this.created = chat.getCreatedInMillis();
        this.id = chat.getId();
        this.message = chat.getMessage();
        this.username = chat.getUsername();
        this.pbfId = chat.getPbfId();
    }
}
