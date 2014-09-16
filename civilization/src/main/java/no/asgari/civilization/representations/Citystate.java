package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.ExcelSheet;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.ObjectId;

@ToString(of = "name")
@Getter
@Setter
@JsonTypeName("citystate")
public class Citystate implements Item {
    @JsonProperty("_id")
    @ObjectId
    private String id;

    @JsonProperty
    @NotEmpty
    private final String name;
    @JsonProperty
    private String type;
    @JsonProperty
    private String description;
    @JsonProperty
    private boolean used;
    @JsonProperty
    private boolean hidden;
    @JsonProperty
    private String owner; // (username)

    public Citystate(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.CITY_STATES;
    }

}
