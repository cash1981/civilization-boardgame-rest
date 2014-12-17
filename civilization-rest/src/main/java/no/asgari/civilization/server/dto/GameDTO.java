package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.Playerhand;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * Json for inside a game.
 * Will have GameLog info and Players stuff
 */
@Data
@JsonRootName("gameDTO")
public class GameDTO {
    @NotEmpty
    private String id;

    @NotBlank
    private String name;
    private GameType type;

    private long created;

    private List<GameLogDTO> publicLogs;
    private List<GameLogDTO> privateLogs;

    private Playerhand player;
}
