package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.SheetName;
import org.mongojack.Id;
import org.mongojack.ObjectId;

/**
 * Type should describe the unit type, for instance
 * Spearmen, Pikemen, Riflemen etc
 */
@Getter
@Setter
@JsonTypeName("aircraft")
@NoArgsConstructor
public class Aircraft implements Unit {
    @ObjectId
    @Id
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

    /**
     * Aircrafts have no level
     * @return 0
     */
    @JsonIgnore
    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.AIRCRAFT;
    }

    @Override
    public String toString() {
        return "Aircraft " + attack + "." + health;
    }
}
