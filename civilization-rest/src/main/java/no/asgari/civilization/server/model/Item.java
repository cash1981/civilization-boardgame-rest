package no.asgari.civilization.server.model;

/**
 * The item you pull for instance Great Person, Wonder, Civ etc
 * Most of them will not have type or description.
 * The @used attribute will determine if the item is available or not
 * ie:
 * <p/>
 * sheet = GREAT_PERSON
 * name = Leonidas
 * type = General
 * description = Battle: Each time you start a battle with fewer units in your battle force than your opponent,
 * your combat bonus is increased by 8 until the end of the battle.
 * used = true
 * hidden = false
 * <p/>
 * sheet = CIV
 * name = America
 * type = null
 * description = null
 * used = false
 * hidden = false;
 * <p/>
 * sheet = VILLAGES
 * name = Barbarian Encampment
 * type = null
 * description = Start of Turn : Choose an empty square not in the outskirts of a city. Place a village token on that square.
 * used = true
 * hidden = true;
 */
public interface Item extends Spreadsheet, Type, Comparable<Spreadsheet> {

    /**
     * Either the username or pbf name, both of which must be unique *
     */
    public String getName();

    public String getOwnerId();

    public void setOwnerId(String owner);

    public boolean isHidden();

    public void setHidden(boolean hidden);

    public boolean isUsed();

    public String getDescription();

}
