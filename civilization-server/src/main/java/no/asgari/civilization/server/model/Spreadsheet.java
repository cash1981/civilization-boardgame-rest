package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.asgari.civilization.server.SheetName;

public interface Spreadsheet {

    @JsonIgnore
    SheetName getSheetName();
}
