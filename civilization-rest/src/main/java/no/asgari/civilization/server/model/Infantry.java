package no.asgari.civilization.server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.server.SheetName;

@Getter
@Setter
@JsonTypeName("infantry")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used"}, callSuper = false)
public class Infantry extends Unit implements Image {
    private int level = LEVEL_1;
    private int attack;
    private int health;
    private String ownerId;
    private boolean hidden = true;
    private boolean used;
    private boolean killed;
    private boolean isInBattle;
    private String image;

    public Infantry(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @JsonIgnore
    @Override
    public String getType() {
        return "Infantry";
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.INFANTRY;
    }

    @Override
    public String revealPublic() {
        switch (level) {
            case LEVEL_1:
                return "Spearmen";
            case LEVEL_2:
                return "Pikemen";
            case LEVEL_3:
                return "Riflemen";
            case LEVEL_4:
                return "Modern Infantry";
            default:
                return "Infantry";
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
                return "Spearmen " + attack + "." + health;
            case LEVEL_2:
                return "Pikemen " + (attack + LEVEL_1) + "." + (health + LEVEL_1);
            case LEVEL_3:
                return "Riflemen " + (attack + LEVEL_2) + "." + (health + LEVEL_2);
            case LEVEL_4:
                return "Modern Infantry " + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Infantry Unknown level " + attack + "." + health;
        }
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @JsonGetter("image")
    @Override
    public String getImage() {
        image = getType() + attack + "." + health + ".png";
        return image;
    }
}
