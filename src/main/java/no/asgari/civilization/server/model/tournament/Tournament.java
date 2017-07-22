package no.asgari.civilization.server.model.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tournament {
    public static final String COL_NAME = "tournament";

    @ObjectId
    @Id
    private String id;

    private String name;
    private int tournamentNumber;
    private List<TournamentPlayer> players = new ArrayList<>();
}
