package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class CivWinnerDTO implements Comparable<CivWinnerDTO> {
    private String civ;
    private long totalWins;
    private long totalAttempt;
    private String percentWin;

    public CivWinnerDTO(String civ, Long totalWins, Long totalAttempt) {
        this.civ = civ;
        if (totalWins == null) {
            this.totalWins = 0;
        } else {
            this.totalWins = totalWins;
        }
        if (totalAttempt == null) {
            this.totalAttempt = 0;
        } else {
            this.totalAttempt = totalAttempt;
        }
        percentWin = percentWin();
    }

    @Override
    public int compareTo(CivWinnerDTO o) {
        int i = Long.compare(this.getTotalWins(), o.getTotalWins());
        if (i != 0) {
            return i;
        }

        return civ.compareTo(o.civ);
    }

    @JsonIgnore
    private String percentWin() {
        if (totalWins == 0 || totalAttempt == 0)
            return "0 %";
        double percent = ((double) totalWins / (double) totalAttempt) * 100;
        double twoDecimals = (double) Math.round(percent * 100) / 100;
        return twoDecimals + " %";
    }
}
