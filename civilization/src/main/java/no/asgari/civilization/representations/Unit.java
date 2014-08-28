package no.asgari.civilization.representations;

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
public interface Unit<T> extends Spreadsheet {

    /** Either the username or pbf name, both of which must be unique **/
    public String getOwner();
    public boolean isHidden();
    public boolean isUsed();
    public boolean isDead();
    public int getLevel();
    public String getId();
    /** For instance Pikemen 3/3*/
    public String getType();
    public int getAttack();
    public int getHealth();
    public T getUnit();

}
