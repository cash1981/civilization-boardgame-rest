package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;
import java.util.List;

/**
 * PBF stands for Play By Forum
 */
@JsonRootName(value="pbf")
@Getter
@Setter
public class PBF {
    private String id;

    private List<Player> players = Lists.newArrayList();
    private List<Item> items = Lists.newArrayList();

    private List<Mounted> mounted = Lists.newArrayList();
    private List<Infantry> infantry = Lists.newArrayList();
    private List<Artillery> artillery = Lists.newArrayList();
    private List<Aircraft> aircraft = Lists.newArrayList();

    @NotBlank
    private String name;
    private GameType type;
    private Date created = new Date();
    private int numOfPlayers;

}
