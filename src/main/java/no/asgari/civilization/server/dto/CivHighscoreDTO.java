package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class CivHighscoreDTO implements Comparable<CivHighscoreDTO> {
    private String civ;
    private long totalWins;
    private long totalAttempt;

    public CivHighscoreDTO(String civ, Long totalWins, Long totalAttempt) {
        this.civ = civ;
        if(totalWins == null) {
            this.totalWins = 0;
        } else {
            this.totalWins = totalWins;
        }
        if(totalAttempt == null) {
            this.totalAttempt = 0;
        } else {
            this.totalAttempt = totalAttempt;
        }
    }

    @Override
    public int compareTo(CivHighscoreDTO o) {
        int i = Long.valueOf(this.getTotalWins()).compareTo(o.getTotalWins());
        if(i != 0) {
            return i;
        }

        return civ.compareTo(o.civ);
    }
}
