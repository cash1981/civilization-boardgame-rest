package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * Each PBF has a playerhand consisting of the player and its items
 */
@Data
@JsonRootName("players")
@NoArgsConstructor
public class Playerhand {
    @NotBlank
    //Can consider using the playerId also
    private String username;

    @NotBlank
    private String playerId;

    private List<Item> items = Lists.newArrayList();
    private List<Unit> units = Lists.newArrayList();
}
