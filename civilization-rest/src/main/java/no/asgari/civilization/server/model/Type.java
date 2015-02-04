package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Citystate.class, name = "citystate"),
        @JsonSubTypes.Type(value = Civ.class, name = "civ"),
        @JsonSubTypes.Type(value = CultureI.class, name = "cultureI"),
        @JsonSubTypes.Type(value = CultureII.class, name = "cultureII"),
        @JsonSubTypes.Type(value = CultureIII.class, name = "cultureIII"),
        @JsonSubTypes.Type(value = GreatPerson.class, name = "greatperson"),
        @JsonSubTypes.Type(value = Hut.class, name = "hut"),
        @JsonSubTypes.Type(value = Tile.class, name = "tile"),
        @JsonSubTypes.Type(value = Village.class, name = "village"),
        @JsonSubTypes.Type(value = Wonder.class, name = "wonder"),
        @JsonSubTypes.Type(value = Infantry.class, name = "infantry"),
        @JsonSubTypes.Type(value = Mounted.class, name = "mounted"),
        @JsonSubTypes.Type(value = Artillery.class, name = "artillery"),
        @JsonSubTypes.Type(value = Aircraft.class, name = "aircraft")
})
@FunctionalInterface
public interface Type {
    public String getType();
}
