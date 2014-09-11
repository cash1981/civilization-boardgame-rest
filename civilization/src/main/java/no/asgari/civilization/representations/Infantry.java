package no.asgari.civilization.representations;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import no.asgari.civilization.ExcelSheet;

/**
 * Type should describe the unit type, for instance
 * Spearmen, Pikemen, Riflemen etc
 */
@Getter
@Setter
@JsonTypeName("aircraft")
public class Infantry implements Unit<Infantry> {
    private final int LEVEL_1 = 1;
    private final int LEVEL_2 = 2;
    private final int LEVEL_3 = 3;
    private final int LEVEL_4 = 4;

    private int level = LEVEL_1;
    private final int attack;
    private final int health;
    private String id;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;

    public Infantry(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public Infantry getUnit() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.INFANTRY;
    }

    @Override
    public String toString() {
        switch (level) {
            case LEVEL_1:
                return "Spearmen " + attack + "." + health;
            case LEVEL_2:
                return "Pikemen " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Riflemen " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Modern Infantry" + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Unknown level" + attack + "." + health;
        }
    }
}
