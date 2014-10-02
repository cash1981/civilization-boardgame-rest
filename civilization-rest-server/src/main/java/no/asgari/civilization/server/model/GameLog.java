package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * This class will be used to store all the actions performed in the game.
 * It can be creating game, joining game, drawing items
 *
 * Typically it will be
 *
 * <14.04.2014 - 14:24 - cash1981 drew Civ Japan - <undo button>
 * <14.04.2014 - 14:25 - cash1981 drew Civ Greece - <undo>
 * <14.04.2014 - 14:25 - cash1981 drew Infantry - <undo>
 * <14.04.2014 - 14:25 - cash1981 drew Mounted - <undo>
 * <14.04.2014 - 14:25 - cash1981 drew Artillery - <undo>
 *
 */
@Data
@JsonRootName("gamelog")
@NoArgsConstructor
public class GameLog {
    public static final String COL_NAME = "gamelog";

    @Id
    @ObjectId
    private String id;

    @NotEmpty
    private String log;

    /** Each log belongs to a pbf **/
    @NotEmpty
    private String pbfId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @NotEmpty
    private String username;

    /** Each log is from a corresponding draw **/
    @NotNull
    private Draw draw;

}
