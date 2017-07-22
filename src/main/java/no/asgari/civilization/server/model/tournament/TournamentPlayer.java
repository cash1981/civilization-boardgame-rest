package no.asgari.civilization.server.model.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.asgari.civilization.server.model.Player;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TournamentPlayer {
    private String playerId;
    private String username;
    private boolean paid;


    public TournamentPlayer(Player player) {
        this.playerId = player.getId();
        this.username = player.getUsername();
    }
}
