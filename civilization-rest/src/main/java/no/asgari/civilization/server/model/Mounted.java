package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

@Getter
@Setter
@JsonTypeName("mounted")
@NoArgsConstructor
public class Mounted extends Unit {
    private int level = LEVEL_1;
    private String ownerId;
    private boolean hidden;
    private boolean used;
    private boolean killed;
    private int attack;
    private int health;

    public Mounted(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @JsonIgnore
    @Override
    public String getType() {
        return "Mounted";
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.MOUNTED;
    }

    @Override
    public String revealPublic() {
        switch (level) {
            case LEVEL_1:
                return "Horsemen";
            case LEVEL_2:
                return "Knight";
            case LEVEL_3:
                return "Cavalry";
            case LEVEL_4:
                return "Tank";
            default:
                return "Mounted";
        }
    }

    @Override
    public String revealAll() {
        return toString();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mounted mounted = (Mounted) o;

        if (attack != mounted.attack) return false;
        if (killed != mounted.killed) return false;
        if (health != mounted.health) return false;
        if (hidden != mounted.hidden) return false;
        if (used != mounted.used) return false;
        if (ownerId != null ? !ownerId.equals(mounted.ownerId) : mounted.ownerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = health;
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (killed ? 1 : 0);
        result = 31 * result + attack;
        return result;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
