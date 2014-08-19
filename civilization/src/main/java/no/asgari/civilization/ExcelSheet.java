package no.asgari.civilization;

import java.util.EnumSet;

public class ExcelSheet {

    public static final EnumSet<Sheet> SHEETS =
            EnumSet.of(Sheet.CIV, Sheet.CULTURE_1, Sheet.CULTURE_2, Sheet.CULTURE_3,
                    Sheet.GREAT_PERSON, Sheet.INFANTRY, Sheet.ARTILLERY, Sheet.MOUNTED, Sheet.AIRCRAFT,
                    Sheet.VILLAGES, Sheet.HUTS, Sheet.WONDERS, Sheet.TILES, Sheet.CITY_STATES);

    public enum Sheet {
        CIV("Civ"), CULTURE_1("Culture I"), CULTURE_2("Culture II"),
        CULTURE_3("Culture III"), GREAT_PERSON("Great Person"), INFANTRY("Infantry"), ARTILLERY("Artillery"), MOUNTED("Mounted"),
        AIRCRAFT("Aircraft"), VILLAGES("Villages"), HUTS("Huts"), WONDERS("Wonders"), TILES("Tiles"), CITY_STATES("City-states");

        private String label;

        Sheet(String name) {
            this.label = name;
        }

        @Override
        public String toString() {
            return label;
        }

    }
}
