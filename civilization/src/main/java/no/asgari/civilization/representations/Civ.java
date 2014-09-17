package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.SheetName;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

@Getter
@Setter
@ToString(of="name")
@JsonTypeName("civ")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "objectType")
@NoArgsConstructor
public class Civ implements Item {
    @NotEmpty
    private String name;
    @ObjectId
    @Id
    private String id;
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


    @JsonIgnore
    @Override
    public SheetName getSheetName() {
        return SheetName.CIV;
    }

}
