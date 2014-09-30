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
@JsonTypeName("aircraft")
@NoArgsConstructor
public class Aircraft implements Unit {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aircraft aircraft = (Aircraft) o;

        if (attack != aircraft.attack) return false;
        if (dead != aircraft.dead) return false;
        if (health != aircraft.health) return false;
        if (hidden != aircraft.hidden) return false;
        if (used != aircraft.used) return false;
        if (owner != null ? !owner.equals(aircraft.owner) : aircraft.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (dead ? 1 : 0);
        result = 31 * result + attack;
        result = 31 * result + health;
        return result;
    }
}
