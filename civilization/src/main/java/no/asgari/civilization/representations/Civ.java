package no.asgari.civilization.representations;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import no.asgari.civilization.ExcelSheet;
import org.hibernate.validator.constraints.NotEmpty;

public class Civ implements Item<Civ> {
    @Id
    @ObjectId
    private String id;
    @NotEmpty
    private final String name;

    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public Civ(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public String getName() {
        return name;
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
    public String getDescription() {
        return description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Civ getItem() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.CIV;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}
