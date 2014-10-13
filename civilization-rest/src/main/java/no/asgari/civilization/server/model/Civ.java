package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of="name")
@JsonTypeName("civ")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "objectType")
@NoArgsConstructor
public class Civ implements Item {
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public Civ(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @JsonIgnore
    @Override
    public SheetName getSheetName() {
        return SheetName.CIV;
    }

    @Override
    public String revealPublic() {
        return getClass().getSimpleName();
    }

    @Override
    public String revealAll() {
        return getClass().getSimpleName() + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Civ civ = (Civ) o;

        if (hidden != civ.hidden) return false;
        if (used != civ.used) return false;
        if (description != null ? !description.equals(civ.description) : civ.description != null) return false;
        if (!name.equals(civ.name)) return false;
        if (owner != null ? !owner.equals(civ.owner) : civ.owner != null) return false;
        if (type != null ? !type.equals(civ.type) : civ.type != null) return false;

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
