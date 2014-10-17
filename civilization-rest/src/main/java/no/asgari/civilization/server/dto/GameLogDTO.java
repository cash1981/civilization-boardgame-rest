package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@JsonRootName("gameLogDTO")
@Data
public class GameLogDTO {
    private String id;
    private String log;
}
