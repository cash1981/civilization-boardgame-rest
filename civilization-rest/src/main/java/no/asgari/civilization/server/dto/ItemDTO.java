package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@JsonRootName("itemDTO")
@Setter
@Getter
@ToString(of = "name")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDTO {
    /**
     * ie: Leonidas *
     */
    @NotEmpty
    private String name;
    /**
     * if it is to be sent to a new playerId
     */
    private String ownerId;
    /**
     * If the item is to be revealed
     */
    private boolean hidden;
    private boolean used;
    private String description;
    /**
     * Type of item, ie Scientist
     */
    private String type;
    /**
     * ie Great Person
     */
    @NotNull
    private String sheetName;

    private String pbfId;
}
