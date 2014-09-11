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
public class Aircraft implements Unit<Aircraft> {
    private String id;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;
    private int attack;
    private int health;

    public Aircraft(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public Aircraft getUnit() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.AIRCRAFT;
    }

    @Override
    public String toString() {
        return "Aircraft " + attack + "." + health;
    }
}
