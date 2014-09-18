package no.asgari.civilization.representations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.asgari.civilization.SheetName;

public interface Spreadsheet {

    @JsonIgnore
    SheetName getSheetName();
}
