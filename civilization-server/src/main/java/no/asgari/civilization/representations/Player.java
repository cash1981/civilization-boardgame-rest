package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Player {
    @JsonIgnore
    public static final String COL_NAME = "player";

    @ObjectId
    @Id
    private String id;

    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;

    /** A list of all the private draws a player has made in one game * */
    private List<Draw> draws = Lists.newArrayList();
    private List<Item> items = Lists.newArrayList();
    /** Set of unique active games * */
    private Set<String> gameIds = new HashSet<>();
    private List<Unit> units = Lists.newArrayList();
}
