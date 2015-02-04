package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import no.asgari.civilization.server.model.GameType;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonRootName("pbfDTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PbfDTO {
    @NotEmpty
    private String id;

    @NotBlank
    private String name;
    private GameType type;

    private long created = System.currentTimeMillis();

    private int numOfPlayers;
    private boolean active = true;

    private List<PlayerDTO> players = new ArrayList<>();
    private String nameOfUsersTurn;

}
