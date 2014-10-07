package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonRootName("player")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    @JsonIgnore public static final String COL_NAME = "player";
    @JsonIgnore public static final String USERNAME = "username";

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

    /** Set of unique active games This may be reduntant as it can be calculated by looping through all pbfs.players and finding match. */
    private Set<String> gameIds = new HashSet<>();
}
