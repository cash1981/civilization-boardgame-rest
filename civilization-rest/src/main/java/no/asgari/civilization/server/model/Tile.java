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
@JsonTypeName("tile")
@NoArgsConstructor
public class Tile implements Item {
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String ownerId; // game_id or player_id (username)

    public Tile(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.TILES;
    }

    @Override
    public String revealPublic() {
        return getClass().getSimpleName() + " " + name;
    }

    @Override
    public String revealAll() {
        return revealPublic();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tile tile = (Tile) o;

        if (hidden != tile.hidden) return false;
        if (used != tile.used) return false;
        if (description != null ? !description.equals(tile.description) : tile.description != null) return false;
        if (!name.equals(tile.name)) return false;
        if (ownerId != null ? !ownerId.equals(tile.ownerId) : tile.ownerId != null) return false;
        if (type != null ? !type.equals(tile.type) : tile.type != null) return false;

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
