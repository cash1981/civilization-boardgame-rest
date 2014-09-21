package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
//@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT, property="type")
//@JsonSubTypes({
//        @JsonSubTypes.Type(value=Infantry.class, name="infantry"),
//        @JsonSubTypes.Type(value=Mounted.class, name="mounted"),
//        @JsonSubTypes.Type(value=Artillery.class, name="artillery"),
//        @JsonSubTypes.Type(value=Aircraft.class, name="aircraft")
//})
public interface Unit extends Spreadsheet {
    @JsonIgnore
    static final int LEVEL_1 = 1;
    @JsonIgnore
    static final int LEVEL_2 = 2;
    @JsonIgnore
    static final int LEVEL_3 = 3;
    @JsonIgnore
    static final int LEVEL_4 = 4;

    /**
     * Either the username or pbf name, both of which must be unique *
     */
    public String getOwner();

    public boolean isHidden();

    public boolean isUsed();

    public boolean isDead();

    public int getLevel();

    public String getId();

    public int getAttack();

    public int getHealth();

}
