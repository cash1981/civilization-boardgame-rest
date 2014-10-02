package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
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
@NoArgsConstructor
@JsonRootName(value = "draw")
@Data
public class Draw<T extends Type> {
    public static final String COL_NAME = "draw";

    @ObjectId
    @Id
    /** Will be used to identify a draw so that voting of undo can be performed **/
    private String id;

    /** Typically implementation of Unit or Item. Should have #getSheetName() to determine the type **/
    private T item;

    public Draw(String pbfId, String playerId) {
        this.pbfId = pbfId;
        this.playerId = playerId;

        created = LocalDateTime.now();
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created;

    /** The user that made the draw. Its always a player that initiates a draw, so this cannot be blank.
     * TODO: Can consider using username instead if that is made unique
     **/
    @NotBlank
    private String playerId;
    /**A draw must always belong to a game. The pbf game id **/
    @NotBlank
    private String pbfId;

}
