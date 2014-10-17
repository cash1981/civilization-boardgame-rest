package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString()
@JsonTypeName("wonder")
@NoArgsConstructor
@EqualsAndHashCode(exclude={"ownerId", "hidden", "used"})
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
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
