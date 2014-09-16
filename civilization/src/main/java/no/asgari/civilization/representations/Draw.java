package no.asgari.civilization.representations;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import no.asgari.civilization.ExcelSheet;
import org.mongojack.ObjectId;

/**
 * Class that handles drawing of things.
 * This class will log all the draws and output them publicly and privatly
 *
 * It will support undo of draws, which will put the item back in the deck and shuffle
 *
 * <T> - Typically implementation of Unit or Item
 *
 */
@Setter
@Getter
public class Draw<T> implements Spreadsheet {

    @JsonProperty("_id")
    @ObjectId
    /** Will be used to identify a draw so that voting of undo can be performed **/
    private String id;

    /** Typically implementation of Unit or Item **/
    private T item;

    private LocalDateTime dateTime;

    private ExcelSheet sheetName;

    /** The user that made the draw **/
    private String userId;
    /** The pbf game id **/
    private String pbfId;
}
