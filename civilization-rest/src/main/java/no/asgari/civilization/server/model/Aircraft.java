package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

@Getter
@Setter
@JsonTypeName("aircraft")
@NoArgsConstructor
//@EqualsAndHashCode(of = {"used", "attack", "health", "type"}, callSuper = true)
public class Aircraft extends Unit {
    private String ownerId; // id of the player which owns this item
    private boolean hidden;
    private boolean used;
    private boolean killed;
    private int attack;
    private int health;
    private boolean isInBattle;

    public Aircraft(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @JsonIgnore
    @Override
    public String getType() {
        return "Aircraft";
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
    public String revealPublic() {
        return getType();
    }

    @Override
    public String revealAll() {
        return toString();
    }

    @Override
    public String toString() {
        return getType() + attack + "." + health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aircraft aircraft = (Aircraft) o;

        if (attack != aircraft.attack) return false;
        if (killed != aircraft.killed) return false;
        if (health != aircraft.health) return false;
        if (hidden != aircraft.hidden) return false;
        if (used != aircraft.used) return false;
        if (isInBattle != aircraft.isInBattle) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (hidden ? 1 : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (killed ? 1 : 0);
        result = 31 * result + attack;
        result = 31 * result + health;
        result = 31 * result + (isInBattle ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
