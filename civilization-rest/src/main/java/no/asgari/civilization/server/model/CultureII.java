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
@ToString(of="name")
@JsonTypeName("cultureII")
@NoArgsConstructor
public class CultureII implements Item, Tradable {
    @NotEmpty
    private String name;
    private String description;
    private String type;
    private boolean used;
    private boolean hidden;
    private String ownerId; // player_id

    public CultureII(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.CULTURE_2;
    }

    @Override
    public String revealPublic() {
        return getClass().getSimpleName();
    }

    @Override
    public String revealAll() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CultureII cultureII = (CultureII) o;

        if (hidden != cultureII.hidden) return false;
        if (used != cultureII.used) return false;
        if (description != null ? !description.equals(cultureII.description) : cultureII.description != null)
            return false;
        if (!name.equals(cultureII.name)) return false;
        if (ownerId != null ? !ownerId.equals(cultureII.ownerId) : cultureII.ownerId != null) return false;
        if (type != null ? !type.equals(cultureII.type) : cultureII.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
