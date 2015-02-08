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
@JsonTypeName("mounted")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used"}, callSuper = false)
public class Mounted extends Unit implements Image {
    private int level = LEVEL_1;
    private String ownerId;
    private boolean hidden = true;
    private boolean used;
    private boolean killed;
    private int attack;
    private int health;
    private boolean isInBattle;
    private String image;

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
                return "Tank " + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Mounted unknown level " + attack + "." + health;
        }
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @JsonGetter("image")
    @Override
    public String getImage() {
        image = getType() + attack + "." + health + PNG;
        return image.replaceAll(" ", "");
    }
}
