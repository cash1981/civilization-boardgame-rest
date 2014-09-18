package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

@Getter
@Setter
@ToString(of="name")
@JsonTypeName("tile")
@NoArgsConstructor
public class Tile implements Item {
    @NotEmpty
    private String name;
    @ObjectId
    @Id
    private String id;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public Tile(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.TILES;
    }

}
