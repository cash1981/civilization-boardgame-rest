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
import java.util.UUID;

@Data
@JsonRootName("player")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    @JsonIgnore
    public static final String COL_NAME = "player";

    @ObjectId
    @Id
    private String id;

    /**Will use this token to authorize the user. This UUID should be cached once the user logs in */
    @JsonIgnore
    private final UUID token = UUID.randomUUID();

    @NotBlank
    //Unique
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;

    /** Set of unique active games * */
    private Set<String> gameIds = new HashSet<>();
}
