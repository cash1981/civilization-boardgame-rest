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
@JsonTypeName("artillery")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used"}, callSuper = false)
public class Artillery extends Unit implements Image {
    private int level = LEVEL_1;
    private String ownerId;
    private boolean hidden = true;
    private boolean used;
    private boolean killed;
    private int attack;
    private int health;
    private boolean isInBattle;
    private String image;

    public Artillery(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @JsonIgnore
    @Override
    public String getType() {
        return "Artillery";
    }

    @JsonIgnore
    @Override
    public SheetName getSheetName() {
        return SheetName.ARTILLERY;
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
        switch (level) {
            case LEVEL_1:
                return "Archer " + attack + "." + health;
            case LEVEL_2:
                return "Cannon " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Catapult " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Mobile Artillery " + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Artillery unknown level " + attack + "." + health;
        }
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @Override
    public String getImage() {
        image = getType() + attack + "." + health + PNG;
        return image.replaceAll(" ", "");
    }
}
