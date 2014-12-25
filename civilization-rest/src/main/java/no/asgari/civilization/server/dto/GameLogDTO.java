package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.model.Undo;

@JsonRootName("gameLogDTO")
@Data
@NoArgsConstructor
public class GameLogDTO {
    private String id;
    private String log;
    private long created;
    private Undo undo;

    public GameLogDTO(String id, String log, long created) {
        this.id = id;
        this.log = log;
        this.created = created;
    }
}
