package no.asgari.civilization.representations;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.ExcelSheet;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString(of="name")
@JsonTypeName("greatperson")
public class GreatPerson implements Item<GreatPerson> {
    @NotEmpty
    private final String name;
    private String id;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden;
    private String owner; // game_id or player_id (username)

    public GreatPerson(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public GreatPerson getItem() {
        return this;
    }

    @Override
    public ExcelSheet getSheetName() {
        return ExcelSheet.GREAT_PERSON;
    }

}
