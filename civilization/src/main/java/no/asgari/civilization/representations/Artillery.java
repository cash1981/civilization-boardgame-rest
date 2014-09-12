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
@JsonTypeName("artillery")
public class Artillery implements Unit {
    public final int LEVEL_1 = 1;
    private int level = LEVEL_1;
    public final int LEVEL_2 = 2;
    public final int LEVEL_3 = 3;
    public final int LEVEL_4 = 4;
    private String id;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;
    private int attack;
    private int health;

    public Artillery(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.ARTILLERY;
    }

    @Override
    public String toString() {
        switch (level) {
            case LEVEL_1:
                return "Archer " + attack + "." + health;
            case LEVEL_2:
                return "Cannon " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Catapult " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Mobile Artillery" + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Unknown level" + attack + "." + health;
        }
    }
}
