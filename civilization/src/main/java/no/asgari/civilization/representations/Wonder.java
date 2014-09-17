package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.SheetName;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

@Getter
@Setter
@ToString(of={"type", "name", "description"})
@JsonTypeName("wonder")
@NoArgsConstructor
public class Wonder implements Item {
    @JsonIgnore
    public static String ANCIENT = "Ancient";
    @JsonIgnore
    public static String MEDIEVAL = "Medieval";
    @JsonIgnore
    public static String MODERN = "Modern";
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

}
