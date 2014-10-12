package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.Spreadsheet;
import org.hibernate.validator.constraints.NotEmpty;

@JsonRootName("itemDTO")
@Setter
@Getter
@ToString(of="name")
public class ItemDTO implements Item {
    /** ie: Leonidas **/
    @NotEmpty
    private String name;
    /**if it is to be sent to a new owner */
    private String owner;
    /** If the item is to be revealed */
    private boolean hidden;
    private boolean used;
    private String description;
    /** Type of item, ie Scientist */
    private String type;
    /** ie Great Person */
    private SheetName sheetName;

    @Override
    public int compareTo(Spreadsheet o) {
        return sheetName.compareTo(o.getSheetName());
    }
}
