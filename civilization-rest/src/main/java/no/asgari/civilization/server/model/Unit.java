package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;

@Setter
public abstract class Unit implements Item, Level {
    String type;
    String name;

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

    @Override
    public String getName() {
        return name = getType() + " " + getAttack() + "." + getHealth();
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
