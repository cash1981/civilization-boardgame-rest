package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@JsonTypeName("cultureI")
@ToString(of="name")
@NoArgsConstructor
public class CultureI implements Item {
    @NotEmpty
    private String name;
    private String description;
    private String type;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public CultureI(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.CULTURE_1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CultureI cultureI = (CultureI) o;

        if (hidden != cultureI.hidden) return false;
        if (used != cultureI.used) return false;
        if (description != null ? !description.equals(cultureI.description) : cultureI.description != null)
            return false;
        if (!name.equals(cultureI.name)) return false;
        if (owner != null ? !owner.equals(cultureI.owner) : cultureI.owner != null) return false;
        if (type != null ? !type.equals(cultureI.type) : cultureI.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
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
