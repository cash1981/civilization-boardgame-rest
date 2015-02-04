package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@JsonTypeName("cultureI")
@ToString(of = "name")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"ownerId", "hidden", "used"})
public class CultureI implements Item, Tradable {
    @NotEmpty
    private String name;
    private String description;
    private String type;
    private boolean used;
    private boolean hidden = true;
    private String ownerId; // player_id

    public CultureI(String name) {
        this.name = name;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public SheetName getSheetName() {
        return SheetName.CULTURE_1;
    }

    @Override
    public String revealPublic() {
        return getClass().getSimpleName();
    }

    @Override
    public String revealAll() {
        return name;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }
}
