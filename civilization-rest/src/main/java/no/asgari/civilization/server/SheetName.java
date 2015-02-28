package no.asgari.civilization.server;

import java.util.EnumSet;
import java.util.Optional;

/**
 * The sheet names of the excel sheet
 */
public enum SheetName {
    CIV("Civ"), CULTURE_1("Culture I"), CULTURE_2("Culture II"),
    CULTURE_3("Culture III"), GREAT_PERSON("Great Person"), INFANTRY("Infantry"), ARTILLERY("Artillery"), MOUNTED("Mounted"),
    AIRCRAFT("Aircraft"), VILLAGES("Villages"), HUTS("Huts"), WONDERS("Wonders"), TILES("Tiles"), CITY_STATES("City-states"),
    LEVEL_1_TECH("Level 1 Tech"), LEVEL_2_TECH("Level 2 Tech"), LEVEL_3_TECH("Level 3 Tech"), LEVEL_4_TECH("Level 4 Tech"), LEVEL_5_TECH("Level 5 Tech");

    private String label;

    SheetName(String name) {
        this.label = name;
    }

    public String getName() {
        return label;
    }

    public static final EnumSet<SheetName> SHEETS =
            EnumSet.of(CIV, CULTURE_1, CULTURE_2, CULTURE_3,
                    GREAT_PERSON, INFANTRY, ARTILLERY, MOUNTED, AIRCRAFT,
                    VILLAGES, HUTS, WONDERS, TILES, CITY_STATES,
                    LEVEL_1_TECH, LEVEL_2_TECH, LEVEL_3_TECH, LEVEL_4_TECH, LEVEL_5_TECH);

    public static final EnumSet<SheetName> TECHS = EnumSet.of(LEVEL_1_TECH, LEVEL_2_TECH, LEVEL_3_TECH, LEVEL_4_TECH, LEVEL_5_TECH);

    public static final EnumSet<SheetName> UNITS = EnumSet.of(AIRCRAFT, ARTILLERY, INFANTRY, MOUNTED);

    public static final EnumSet<SheetName> SHUFFLABLE_ITEMS = EnumSet.of(AIRCRAFT, ARTILLERY, INFANTRY, MOUNTED,
            GREAT_PERSON, CULTURE_1, CULTURE_2, CULTURE_3);

    public static Optional<SheetName> find(String name) {
        String spacesRemovedName = name.replaceAll("\\s", "");
        Optional<SheetName> found = SHEETS.stream()
                .filter(sheet -> sheet.label.replaceAll("\\s", "").equalsIgnoreCase(spacesRemovedName))
                .findFirst();
        if (!found.isPresent()) {
            try {
                return Optional.of(valueOf(name));
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
        return found;
    }
}
