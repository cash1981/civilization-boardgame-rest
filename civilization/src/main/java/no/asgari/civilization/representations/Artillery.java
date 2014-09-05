package no.asgari.civilization.representations;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import no.asgari.civilization.ExcelSheet;

/**
 * Type should describe the unit type, for instance
 * Spearmen, Pikemen, Riflemen etc
 */
public class Artillery implements Unit<Artillery> {
    public final int LEVEL_1 = 1;
    public final int LEVEL_2 = 2;
    public final int LEVEL_3 = 3;
    public final int LEVEL_4 = 4;

    @Id
    @ObjectId
    private String id;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;
    private int level = LEVEL_1;
    private int attack;
    private int health;

    public Artillery(int attack, int health) {
        this.attack = attack;
        this.health = health;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean isUsed() {
        return used;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getAttack() {
        return attack;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public Artillery getUnit() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.ARTILLERY;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public void setHealth(int health) {
        this.health = health;
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
                return "Mobile Artillery" + (attack + LEVEL_3) + "." + (health + LEVEL_3);
            default:
                return "Unknown level" + attack + "." + health;
        }
    }
}
