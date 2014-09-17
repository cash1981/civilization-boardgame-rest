package no.asgari.civilization;

import java.util.EnumSet;
import java.util.Optional;

/**
 * The sheet names of the excel sheet
 */
public enum SheetName {
    CIV("Civ"), CULTURE_1("Culture I"), CULTURE_2("Culture II"),
    CULTURE_3("Culture III"), GREAT_PERSON("Great Person"), INFANTRY("Infantry"), ARTILLERY("Artillery"), MOUNTED("Mounted"),
    AIRCRAFT("Aircraft"), VILLAGES("Villages"), HUTS("Huts"), WONDERS("Wonders "), TILES("Tiles"), CITY_STATES("City-states");
    //FIXME Extra space in wonders sheet. Must remove it, and remove space here
    private String label;

    SheetName(String name) {
        this.label = name;
    }

    @Override
    public String toString() {
        return label;
    }

    public static final EnumSet<SheetName> SHEETS =
            EnumSet.of(SheetName.CIV, SheetName.CULTURE_1, SheetName.CULTURE_2, SheetName.CULTURE_3,
                    SheetName.GREAT_PERSON, SheetName.INFANTRY, SheetName.ARTILLERY, SheetName.MOUNTED, SheetName.AIRCRAFT,
                    SheetName.VILLAGES, SheetName.HUTS, SheetName.WONDERS, SheetName.TILES, SheetName.CITY_STATES);

    public static Optional<SheetName> find(String name) {
        return SHEETS.stream()
                .filter(sheet -> sheet.label.equals(name.trim()))
                .findFirst();
    }
}
