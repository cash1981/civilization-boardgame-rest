package no.asgari.civilization.server.model;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

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

/**
 * This class will be used to store all the actions performed in the game.
 * It can be creating game, joining game, drawing items
 *
 * Example of log can be
 * Public view:
 * <14.04.2014 - 14:25 - cash1981 drew Infantry
 * <14.04.2014 - 14:25 - cash1981 drew Mounted
 * <14.04.2014 - 14:25 - cash1981 drew Artillery
 *
 */
@Data
@JsonRootName("privateLog")
@NoArgsConstructor
public class PublicLog {
    public static final String COL_NAME = "publicLog";

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
     * Creates the log and sets it in the log field
     */
    @JsonIgnore
    public void createAndSetLog() {
        final String SPACE = " - ";
        StringBuilder sb = new StringBuilder();
        sb.append(created + SPACE );
        sb.append(username + SPACE);
        sb.append("drew " + SPACE);
        sb.append(draw.getItem().getSheetName());
        //TODO Need to implement better here. Citystate for instance we need to know name
        //Great Person we need to know type, so there are some differences
        //Unit we only want type
        log = sb.toString();
    }
}
