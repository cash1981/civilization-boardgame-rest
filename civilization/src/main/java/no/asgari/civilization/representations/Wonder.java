package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.ExcelSheet;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of={"type", "name", "description"})
@JsonTypeName("wonder")
public class Wonder implements Item<Wonder> {
    public static String ANCIENT = "Ancient";
    public static String MEDIEVAL = "Medieval";
    public static String MODERN = "Modern";
    @NotEmpty
    private final String name;
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
    public Wonder getItem() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.WONDERS;
    }

}
