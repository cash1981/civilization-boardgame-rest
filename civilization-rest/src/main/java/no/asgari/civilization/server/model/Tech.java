package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * All the Level 1, 2, 3, 4 and Space Flight techs
 */
@JsonRootName("tech")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"name"})
public class Tech implements Item, Level, Image {
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
    private String ownerId;
    private int level;
    private String image;
    private SheetName sheetName;

    public Tech(String name, int level) {
        this.name = name;
        this.level = level;
        hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        switch (this.level) {
            case LEVEL_1:
                return sheetName = SheetName.LEVEL_1_TECH;
            case LEVEL_2:
                return sheetName = SheetName.LEVEL_2_TECH;
            case LEVEL_3:
                return sheetName = SheetName.LEVEL_3_TECH;
            case LEVEL_4:
                return sheetName = SheetName.LEVEL_4_TECH;
            case LEVEL_5:
                return sheetName = SheetName.LEVEL_5_TECH;
        }

        throw new IllegalArgumentException("Unknown tech level " + level);
    }

    @Override
    public String revealPublic() {
        return getSheetName().getName();
    }

    @Override
    public String revealAll() {
        return name;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @JsonGetter("image")
    @Override
    public String getImage() {
        image = name + ".png";
        return image.replaceAll(" ", "");
    }
}
