package no.asgari.civilization;

import java.util.EnumSet;
import java.util.Optional;

public enum ExcelSheet {
    CIV("Civ"), CULTURE_1("Culture I"), CULTURE_2("Culture II"),
    CULTURE_3("Culture III"), GREAT_PERSON("Great Person"), INFANTRY("Infantry"), ARTILLERY("Artillery"), MOUNTED("Mounted"),
    AIRCRAFT("Aircraft"), VILLAGES("Villages"), HUTS("Huts"), WONDERS("Wonders"), TILES("Tiles"), CITY_STATES("City-states");

    private String label;

    ExcelSheet(String name) {
        this.label = name;
    }

    @Override
    public String toString() {
        return label;
    }

    public static final EnumSet<ExcelSheet> SHEETS =
            EnumSet.of(ExcelSheet.CIV, ExcelSheet.CULTURE_1, ExcelSheet.CULTURE_2, ExcelSheet.CULTURE_3,
                    ExcelSheet.GREAT_PERSON, ExcelSheet.INFANTRY, ExcelSheet.ARTILLERY, ExcelSheet.MOUNTED, ExcelSheet.AIRCRAFT,
                    ExcelSheet.VILLAGES, ExcelSheet.HUTS, ExcelSheet.WONDERS, ExcelSheet.TILES, ExcelSheet.CITY_STATES);

    public static Optional<ExcelSheet> find(String name) {
        return SHEETS.stream()
                .filter(sheet -> sheet.label.equals(name.trim()))
                .findFirst();
    }
}
