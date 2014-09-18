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
@JsonTypeName("cultureI")
@ToString(of="name")
@NoArgsConstructor
public class CultureI implements Item {
    @NotEmpty
    private String name;
    @ObjectId
    @Id
    private String id;
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

}
