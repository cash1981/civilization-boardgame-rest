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
public abstract class Unit implements Item, Level {
    @JsonIgnore
    static final int LEVEL_1 = 1;
    @JsonIgnore
    static final int LEVEL_2 = 2;
    @JsonIgnore
    static final int LEVEL_3 = 3;
    @JsonIgnore
    static final int LEVEL_4 = 4;

    public abstract boolean isKilled();

    public abstract int getAttack();

    public abstract int getHealth();

    @JsonIgnore
    @Override
    public String getName() {
        return getType();
    }

    @JsonIgnore
    @Override
    public String getDescription() {
        return getType();
    }

    @Override
    public String revealPublic() {
        return getType();
    }

    @Override
    public String revealAll() {
        return toString();
    }

    public abstract boolean isInBattle();

    public abstract void setInBattle(boolean inBattle);
}
