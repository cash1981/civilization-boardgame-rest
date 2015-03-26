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
import java.time.ZoneId;

@Data
@JsonRootName("chat")
@NoArgsConstructor
public class Chat {
    public static final String COL_NAME = "chat";
    public static final String PBFID = "pbfId";

    @Id
    @ObjectId
    private String id;

    @NotNull
    private String pbfId;

    @NotNull
    private String username;

    @NotEmpty
    private String message;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created = LocalDateTime.now();

    @JsonIgnore
    public long getCreatedInMillis() {
        return created.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
