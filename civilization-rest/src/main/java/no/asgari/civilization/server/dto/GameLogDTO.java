package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@JsonRootName("gameLogDTO")
@Data
public class GameLogDTO {
    private String id;
    private String log;

    public GameLogDTO(String id, String log) {
        this.id = id;
        this.log = log;

    }
}
