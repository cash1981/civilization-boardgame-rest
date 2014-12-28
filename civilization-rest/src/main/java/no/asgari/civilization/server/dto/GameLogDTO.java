package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.Undo;

@JsonRootName("gameLogDTO")
@Data
@NoArgsConstructor
public class GameLogDTO {
    private String id;
    private String log;
    private long created;
    private Draw draw;

    public GameLogDTO(String id, String log, long created, Draw draw) {
        this.id = id;
        this.log = log;
        this.created = created;
        this.draw = draw;
    }
}
