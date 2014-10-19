package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import no.asgari.civilization.server.model.GameType;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@JsonRootName(value="CreateNewGame")
public class CreateNewGameDTO {
    @NotEmpty
    public String name;

    @NotNull
    private GameType type;

    @NotNull
    @Min(1)
    private Integer numOfPlayers;
}
