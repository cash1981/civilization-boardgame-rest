package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@ToString()
@JsonTypeName("socialpolicy")
@NoArgsConstructor
@EqualsAndHashCode(of = {"name"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialPolicy implements Item, Image {
    @NotEmpty
    private String name;
    private String type;
    private String description;
    private boolean used;
    private boolean hidden = true;
    private String ownerId;
    private SheetName sheetName;
    private int itemNumber;
    private String image;
    private String flipside;

    public SocialPolicy(String name) {
        this.name = name;
        this.sheetName = SheetName.SOCIAL_POLICY;
        this.used = false;
        this.hidden = true;
    }

    @Override
    public String revealPublic() {
        return name;
    }

    @Override
    public String revealAll() {
        return name;
    }

    @Override
    public int compareTo(Spreadsheet o) {
        return getSheetName().compareTo(o.getSheetName());
    }

    @Override
    public String getImage() {
        image = name + PNG;
        return image.replaceAll(" ", "");
    }
}
