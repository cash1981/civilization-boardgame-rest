package no.asgari.civilization.server.dto;

import java.util.List;

import lombok.Data;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.Playerhand;

@Data
public class GameDTO {

    private String id;
    private GameType type;
    private String name;
    private long created;

    private List<GameLogDTO> publicLogs;
    private List<GameLogDTO> privateLogs;
    private Playerhand player;

}
