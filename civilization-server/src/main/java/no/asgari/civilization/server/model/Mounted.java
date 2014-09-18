package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
@JsonTypeName("mounted")
@NoArgsConstructor
public class Mounted implements Unit {
    @JsonIgnore
    public static final int LEVEL_1 = 1;
    @JsonIgnore
    public static final int LEVEL_2 = 2;
    @JsonIgnore
    public static final int LEVEL_3 = 3;
    @JsonIgnore
    public static final int LEVEL_4 = 4;

    @ObjectId
    @Id
    private String id;

    private int level = LEVEL_1;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;
    private int attack;
    private int health;

    public Mounted(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.MOUNTED;
    }

    @Override
    public String toString() {
        switch (level) {
            case LEVEL_1:
                return "Horsemen " + attack + "." + health;
            case LEVEL_2:
                return "Knight " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Cavalry " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Tank" + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Unknown level" + attack + "." + health;
        }
    }
}
