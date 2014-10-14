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
@ToString(of={"name", "type"})
@JsonTypeName("greatperson")
@NoArgsConstructor
public class GreatPerson implements Item {
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String ownerId; // game_id or player_id (username)

    public GreatPerson(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.GREAT_PERSON;
    }

    @Override
    public String revealPublic() {
        return type;
    }

    @Override
    public String revealAll() {
        return name + " " + type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreatPerson that = (GreatPerson) o;

        if (hidden != that.hidden) return false;
        if (used != that.used) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!name.equals(that.name)) return false;
        if (ownerId != null ? !ownerId.equals(that.ownerId) : that.ownerId != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return 0;
    }
}
