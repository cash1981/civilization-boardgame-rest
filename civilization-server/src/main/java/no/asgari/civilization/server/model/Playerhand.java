package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.List;

@Data
@JsonRootName("players")
@NoArgsConstructor
public class Playerhand {
    @ObjectId
    @Id
    private String id;

    @NotBlank
    //Can consider using the playerId also
    private String username;

    @NotBlank
    private String playerId;

    private List<Item> items = Lists.newArrayList();
    private List<Unit> units = Lists.newArrayList();
}
