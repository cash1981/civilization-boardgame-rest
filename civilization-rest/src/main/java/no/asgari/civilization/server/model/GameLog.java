package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * This class will be used to store all the actions performed in the game.
 * It can be creating game, joining game, drawing items
 * <p/>
 * All private logs/draws can be requested undo
 * <p/>
 * Private view:
 * 14.04.2014 - 14:24 - cash1981 drew Civ Japan - <hidden button> - <undo button>
 * 14.04.2014 - 14:25 - cash1981 drew Civ Greece - <hidden button> - <undo button>
 * <p/>
 * Public view:
 * <14.04.2014 - 14:25 - cash1981 drew Infantry
 * <14.04.2014 - 14:25 - cash1981 drew Mounted
 * <14.04.2014 - 14:25 - cash1981 drew Artillery
 */
@JsonRootName("gameLog")
@NoArgsConstructor
@ToString(of = {"privateLog", "publicLog"})
@Data
public class GameLog {
    public static final String COL_NAME = "gamelog";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public enum LogType {
        TRADE, BATTLE, ITEM, TECH, SHUFFLE, DISCARD, WITHDRAW, JOIN, REVEAL, UNDO, VOTE
    }

    @Id
    @ObjectId
    private String id;

    private String privateLog;

    private String publicLog;

    /**
     * Each log belongs to a pbf
     */
    @NotEmpty
    private String pbfId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @NotEmpty
    private String username;

    /**
     * If log is from a draw
     */
    private Draw draw;

    @JsonIgnore
    public long getCreatedInMillis() {
        return created.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @JsonIgnore
    public void createAndSetLog(LogType logType) {
        final String DELIM = " - ";
        switch (logType) {
            case ITEM:
                privateLog = username + " drew " + DELIM + draw.getItem().revealAll();
                publicLog = username + " drew " + DELIM + draw.getItem().revealPublic();
                break;
            case BATTLE:
                privateLog = username + " plays " + DELIM + draw.getItem().revealAll();
                publicLog = username + " reveals " + DELIM + draw.getItem().revealPublic();
                break;
            case TRADE:
                privateLog = username + " has received from trade " + DELIM + draw.getItem().revealAll();
                publicLog = username + " has received from trade " + DELIM + draw.getItem().revealPublic();
                break;
            case TECH:
                privateLog = username + " has researched  " + DELIM + draw.getItem().revealAll();
                publicLog = username + " has researched " + DELIM + draw.getItem().revealPublic();
                break;
            case DISCARD:
                privateLog = username + " has discarded  " + DELIM + draw.getItem().revealAll();
                publicLog = username + " has discarded " + DELIM + draw.getItem().revealAll();
                break;
            case REVEAL:
                privateLog = username + " has revealed " + DELIM + draw.getItem().revealAll();
                publicLog = username + " has revealed " + DELIM + draw.getItem().revealAll();
                break;
            case UNDO:
                privateLog = username + " has requested undo of " + DELIM + draw.getItem().revealAll();
                publicLog = username + " has requested undo of " + DELIM + draw.getItem().revealAll();
                break;
        }
    }

    @JsonIgnore
    public boolean hasUndo() {
        return draw != null && draw.getUndo() != null;
    }

    @JsonIgnore
    public boolean hasActiveUndo() {
        return draw != null && draw.getUndo() != null && !draw.getUndo().isDone();
    }
}
