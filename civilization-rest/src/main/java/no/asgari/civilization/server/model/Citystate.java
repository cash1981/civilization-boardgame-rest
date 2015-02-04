package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@ToString(of = "name")
@Getter
@Setter
@JsonTypeName("citystate")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used"}, callSuper = false)
public class Citystate implements Item {
    @JsonProperty
    @NotEmpty
    private String name;

    @JsonProperty
    private String type;
    @JsonProperty
    private String description;
    @JsonProperty
    private boolean used;
    @JsonProperty
    private boolean hidden = true;
    @JsonProperty
    private String ownerId; // (playerId)

    public Citystate(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.CITY_STATES;
    }

    @Override
    public String revealPublic() {
        return "City state: " + name;
    }

    @Override
    public String revealAll() {
        return revealPublic();
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
