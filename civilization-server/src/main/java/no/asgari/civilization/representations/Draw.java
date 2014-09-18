package no.asgari.civilization.representations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.asgari.civilization.SheetName;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.time.LocalDateTime;

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
@NoArgsConstructor
public class Draw<T> implements Spreadsheet {

    @ObjectId
    @Id
    /** Will be used to identify a draw so that voting of undo can be performed **/
    private String id;

    /** Typically implementation of Unit or Item **/
    private T item;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created;

    private SheetName sheetName;

    /** The user that made the draw **/
    private String userId;
    /** The pbf game id **/
    private String pbfId;
}
