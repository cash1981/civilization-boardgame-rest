package no.asgari.civilization.representations;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import no.asgari.civilization.ExcelSheet;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * The item you pull for instance Great Person, Wonder, Civ etc
 * Most of them will not have type or description.
 * The @used attribute will determine if the item is available or not
 * ie:
 *
 * sheet = GREAT_PERSON
 * name = Leonidas
 * type = General
 * description = Battle: Each time you start a battle with fewer units in your battle force than your opponent,
 *               your combat bonus is increased by 8 until the end of the battle.
 * used = true
 * hidden = false
 *
 * sheet = CIV
 * name = America
 * type = null
 * description = null
 * used = false
 * hidden = false;
 *
 * sheet = VILLAGES
 * name = Barbarian Encampment
 * type = null
 * description = Start of Turn : Choose an empty square not in the outskirts of a city. Place a village token on that square.
 * used = true
 * hidden = true;
 *
 */
public class Item implements Comparable<Item> {

    @Id
    @ObjectId
    private String id;

    @NotNull
    private final ExcelSheet excelSheet;
    @NotBlank
    private final String name;

    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public Item(ExcelSheet excelSheet, String name) {
        this.excelSheet = excelSheet;
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public ExcelSheet getExcelSheet() {
        return excelSheet;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(Item o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", excelSheet=" + excelSheet +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", used=" + used +
                ", hidden=" + hidden +
                ", owner='" + owner + '\'' +
                '}';
    }
}
