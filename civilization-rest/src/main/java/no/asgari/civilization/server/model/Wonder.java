package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of={"type", "name", "description"})
@JsonTypeName("wonder")
@NoArgsConstructor
public class Wonder implements Item {
    @JsonIgnore
    public static final String ANCIENT = "Ancient";
    @JsonIgnore
    public static final String MEDIEVAL = "Medieval";
    @JsonIgnore
    public static final String MODERN = "Modern";
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String ownerId; // game_id or player_id (username)

    public Wonder(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.WONDERS;
    }

    @Override
    public String revealPublic() {
        return name;
    }

    @Override
    public String revealAll() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wonder wonder = (Wonder) o;

        if (hidden != wonder.hidden) return false;
        if (used != wonder.used) return false;
        if (description != null ? !description.equals(wonder.description) : wonder.description != null) return false;
        if (!name.equals(wonder.name)) return false;
        if (ownerId != null ? !ownerId.equals(wonder.ownerId) : wonder.ownerId != null) return false;
        if (type != null ? !type.equals(wonder.type) : wonder.type != null) return false;

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
        return getSheetName().compareTo(o.getSheetName());
    }
}
