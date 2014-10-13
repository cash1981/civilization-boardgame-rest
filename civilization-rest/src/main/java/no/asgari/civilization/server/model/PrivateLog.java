package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * All private logs/draws can be requested undo
 *
 * Private view:
 * 14.04.2014 - 14:24 - cash1981 drew Civ Japan - <reveal button> - <undo button>
 * 14.04.2014 - 14:25 - cash1981 drew Civ Greece - <reveal button> - <undo button>
 *
 */
@Data
@JsonRootName("privateLog")
@NoArgsConstructor
public class PrivateLog {
    public static final String COL_NAME = "privateLog";

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

    /**
     * All logs which are revealed, will be put in the public log
     */
    private boolean reveal = false;

    @JsonIgnore
    public void createAndSetLog() {
        final String SPACE = " - ";
        StringBuilder sb = new StringBuilder();
        sb.append(created + SPACE );
        sb.append(username + SPACE);
        sb.append("drew " + SPACE + draw.getItem().revealAll());
        log = sb.toString();
    }
}
