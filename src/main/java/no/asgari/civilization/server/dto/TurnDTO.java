package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@JsonRootName("turn")
public class TurnDTO {
    private int turnNumber;
    @NotEmpty
    private String order;
    @NotNull
    private String phase;
    private boolean locked;
}
