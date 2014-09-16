package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * PBF stands for Play By Forum
 */
@JsonRootName(value="pbf")
@Getter
@Setter
public class PBF {
    @ObjectId
    @JsonProperty("_id")
    private String id;

    private List<Player> players = Lists.newArrayList();
    private List<Civ> civs = Lists.newArrayList();
    private List<Citystate> citystates = Lists.newArrayList();
    private List<CultureI> cultureIs = Lists.newArrayList();
    private List<CultureII> cultureIIs = Lists.newArrayList();
    private List<CultureIII> cultureIIIs = Lists.newArrayList();
    private List<GreatPerson> greatPersons = Lists.newArrayList();
    private List<Hut> huts = Lists.newArrayList();
    private List<Tile> tiles = Lists.newArrayList();
    private List<Village> villages = Lists.newArrayList();
    private List<Wonder> wonders = Lists.newArrayList();

    private List<Mounted> mounted = Lists.newArrayList();
    private List<Infantry> infantry = Lists.newArrayList();
    private List<Artillery> artillery = Lists.newArrayList();
    private List<Aircraft> aircraft = Lists.newArrayList();

    @NotBlank
    private String name;
    private Date created = new Date();
    private int numOfPlayers;


}
