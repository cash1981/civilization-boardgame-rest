package no.asgari.civilization.entity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The item you pull for instance Great Person, Wonder, Civ etc
 * Most of them will not have type or description.
 * The @used attribute will determine if the item is available or not
 * ie:
 *
 * name = Leonidas
 * type = General
 * description = Battle: Each time you start a battle with fewer units in your battle force than your opponent,
 *               your combat bonus is increased by 8 until the end of the battle.
 * used = true
 * hidden = false
 *
 * name = America
 * type = null
 * description = null
 * used = false
 * hidden = false;
 *
 * name = Barbarian Encampment
 * type = null
 * description = Start of Turn : Choose an empty square not in the outskirts of a city. Place a village token on that square.
 * used = true
 * hidden = true;
 *
 */
@Entity
@Table(name = "item")
@NamedQueries({
        @NamedQuery(
                name = "no.asgari.civilization.entity.Item.findAll",
                query = "SELECT it FROM Item it"
        )
})
public class Item {

    private String name;
    private String type;
    private String description;
    private boolean used = false;
    private boolean hidden = false;

    private Owner owner;

    public Item() {}

    public Item(String name) {
        this(name, null, null);
    }

    public Item(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}
