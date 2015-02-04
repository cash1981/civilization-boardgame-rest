package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.Playerhand;

import java.util.List;

/**
 * DTO for inside a specific game
 */
@Data
@JsonRootName("gameDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameDTO {

    private String id;
    private GameType type;
    private String name;
    private long created;
    private String whosTurnIsIt; //username of the players turn

    private List<GameLogDTO> publicLogs;
    private List<GameLogDTO> privateLogs;
    private Playerhand player;

}
