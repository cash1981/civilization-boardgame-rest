package no.asgari.civilization.representations;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import no.asgari.civilization.ExcelSheet;

/**
 * Type should describe the unit type, for instance
 * Spearmen, Pikemen, Riflemen etc
 */
public class Aircraft implements Unit<Aircraft> {
    @Id
    @ObjectId
    private String id;
    private String owner;
    private boolean hidden;
    private boolean used;
    private boolean dead;
    private int attack;
    private int health;

    public Aircraft(int attack, int health) {
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
        return 0;
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
    public Aircraft getUnit() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.AIRCRAFT;
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
        return "Aircraft " + attack + "." + health;
    }
}
