package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Class that handles drawing of things.
 * This class will log all the draws and output them publicly and privately
 *
 * It will support undo of draws, which will put the item back in the deck and shuffle
 * Each draw will have a collection of Undo, which contains information about a possible undo with votes and outcome
 *
 * <T> - Typically implementation of Unit or Item
 *
 */
@NoArgsConstructor
@JsonRootName(value = "draw")
@Data
public class Draw<T extends Type> {
    public static final String COL_NAME = "draw";

    /** Will be used to identify a draw so that voting of undo can be performed */
    @ObjectId
    @Id
    private String id;

    /** Typically implementation of Unit or Item. Should have #getSheetName() to determine the type */
    @NotNull
    private T item;

    /** Used to reveal draw, which will make it public */
    private boolean reveal = false;

    public Draw(String pbfId, String playerId) {
        this.pbfId = pbfId;
        this.playerId = playerId;

        created = LocalDateTime.now();
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created;

    /** The user that made the draw. Its always a player that initiates a draw, so this cannot be blank. */
    @NotBlank
    private String playerId;
    /**A draw must always belong to a game. The pbf game id */
    @NotBlank
    private String pbfId;

    /** If null, then no undo has been performed */
    private Undo undo = null;

    /**
     * Returns true if undo has been requested
     */
    @JsonIgnore
    private boolean isUndoInitiated() {
        return undo != null;
    }

}
