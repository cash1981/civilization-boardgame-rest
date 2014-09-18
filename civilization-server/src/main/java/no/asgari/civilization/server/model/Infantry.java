package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;
import org.mongojack.Id;
import org.mongojack.ObjectId;

/**
 * Type should describe the unit type, for instance
 * Spearmen, Pikemen, Riflemen etc
 */
@Getter
@Setter
@JsonTypeName("infantry")
@NoArgsConstructor
public class Infantry implements Unit {
    @ObjectId
    @Id
    private String id;

    private int level = LEVEL_1;
    private int attack;
    private int health;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;

    public Infantry(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.INFANTRY;
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
