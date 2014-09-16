package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.List;

@Getter
@Setter
public class Player {

    @ObjectId
    @JsonProperty("_id")
    private String id;

    @NotBlank
    private String username;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private List<Item> items = Lists.newArrayList();
    private List<Item> units = Lists.newArrayList();
    /** List of active games **/
    private List<String> gameIds = Lists.newArrayList();
}
