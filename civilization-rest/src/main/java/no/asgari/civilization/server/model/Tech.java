package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * All the Level 1, 2, 3, 4 and Space Flight techs
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "level"})

public class Tech implements Item, Level {
    @JsonIgnore
    public static final int LEVEL_1 = 1;
    @JsonIgnore
    public static final int LEVEL_2 = 2;
    @JsonIgnore
    public static final int LEVEL_3 = 3;
    @JsonIgnore
    public static final int LEVEL_4 = 4;
    @JsonIgnore
    public static final int LEVEL_5 = 5;

    @JsonIgnore
    public static final Tech SPACE_FLIGHT = new Tech("Space Flight", LEVEL_5);

    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden = true;
    private String ownerId; // player_id
    private int level;

    public Tech(String name, int level) {
        this.name = name;
        this.level = level;
        hidden = true;
    }

    @JsonIgnore
    @Override
    public SheetName getSheetName() {
        switch (getLevel()) {
            case LEVEL_1:
                return SheetName.LEVEL_1_TECH;
            case LEVEL_2:
                return SheetName.LEVEL_2_TECH;
            case LEVEL_3:
                return SheetName.LEVEL_3_TECH;
            case LEVEL_4:
                return SheetName.LEVEL_4_TECH;
        }

        return SheetName.LEVEL_1_TECH;
    }

    @Override
    public String revealPublic() {
        return getSheetName().toString();
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
