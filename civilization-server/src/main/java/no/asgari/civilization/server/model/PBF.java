package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.common.collect.Lists;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PBF stands for Play By Forum
 */
@Data
@JsonRootName(value="pbf")
@JsonInclude
@JsonIgnoreProperties(ignoreUnknown = true) //Perhaps use this when the object keep getting changed, otherwise jackson throws exception when it cannot map
public class PBF {
    @JsonIgnore
    public static final String COL_NAME = "pbf";

    @ObjectId
    @Id
    private String id;

    @NotBlank
    private String name;
    private GameType type;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    private int numOfPlayers;
    private boolean active = true;

    private List<Playerhand> players = Lists.newArrayList();
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

}
