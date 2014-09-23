package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("player")
@NoArgsConstructor
public class Player {
    @JsonIgnore
    public static final String COL_NAME = "player";

    @ObjectId
    @Id
    private String id;

    @NotBlank
    //Unique
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;

    private List<Item> items = Lists.newArrayList();
    /** Set of unique active games * */
    private Set<String> gameIds = new HashSet<>();
    private List<Unit> units = Lists.newArrayList();

}
