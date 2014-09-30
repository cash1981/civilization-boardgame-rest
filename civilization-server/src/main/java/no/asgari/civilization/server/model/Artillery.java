package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

/**
 * Type should describe the unit type, for instance
 * Spearmen, Pikemen, Riflemen etc
 */
@Getter
@Setter
@JsonTypeName("artillery")
@NoArgsConstructor
public class Artillery implements Unit {
    private int level = LEVEL_1;
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

    @JsonIgnore
    @Override
    public SheetName getSheetName() {
        return SheetName.ARTILLERY;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artillery artillery = (Artillery) o;

        if (attack != artillery.attack) return false;
        if (dead != artillery.dead) return false;
        if (health != artillery.health) return false;
        if (hidden != artillery.hidden) return false;
        if (used != artillery.used) return false;
        if (owner != null ? !owner.equals(artillery.owner) : artillery.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attack;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (dead ? 1 : 0);
        result = 31 * result + health;
        return result;
    }
}
