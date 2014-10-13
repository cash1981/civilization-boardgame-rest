package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
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
    private boolean hidden;
    @JsonProperty
    private String owner; // (username)

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Citystate citystate = (Citystate) o;

        if (hidden != citystate.hidden) return false;
        if (used != citystate.used) return false;
        if (description != null ? !description.equals(citystate.description) : citystate.description != null)
            return false;
        if (!name.equals(citystate.name)) return false;
        if (owner != null ? !owner.equals(citystate.owner) : citystate.owner != null) return false;
        if (type != null ? !type.equals(citystate.type) : citystate.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
