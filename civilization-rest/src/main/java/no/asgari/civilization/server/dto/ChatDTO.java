package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import no.asgari.civilization.server.model.Chat;

@JsonRootName("chat")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ChatDTO {

    private final String id;
    private final String pbfId;
    private final String username;
    private final String message;
    private final long created;

    public ChatDTO(String id, String pbfId, String username, String message, long createdInMillis) {
        this.id = id;
        this.pbfId = pbfId;
        this.username = username;
        this.message = message;
        this.created = createdInMillis;
    }

    public ChatDTO(Chat chat) {
        this.created = chat.getCreatedInMillis();
        this.id = chat.getId();
        this.message = chat.getMessage();
        this.username = chat.getUsername();
        this.pbfId = chat.getPbfId();
    }
}
