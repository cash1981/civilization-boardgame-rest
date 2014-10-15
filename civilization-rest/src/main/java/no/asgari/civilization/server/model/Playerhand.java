package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;
import java.util.Set;

/**
 * Each PBF has a playerhand consisting of the player and its items
 */
@Data
@JsonRootName("players")
@NoArgsConstructor
@EqualsAndHashCode(of={"username", "playerId"})
public class Playerhand {
    @NotBlank
    //Can consider using the playerId instead or removing @NotBlank
    private String username;

    @NotBlank
    private String playerId;

    //Only one starting player each turn
    /** Determines whos turn it is each round **/
    private boolean yourTurn = false;

    private List<Item> items = Lists.newArrayList();
    private Set<Tech> techsChosen = Sets.newTreeSet();


}
