package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class WinnerDTO implements Comparable<WinnerDTO> {
    private String username;
    private int totalWins;
    private long attempts;
    private String percentWin;

    public WinnerDTO(String username, int totalWins, long attempts) {
        this.username = username;
        this.totalWins = totalWins;
        this.attempts = attempts;
        this.percentWin = percentWin();
    }

    @Override
    public int compareTo(WinnerDTO o) {
        int i = Integer.valueOf(this.getTotalWins()).compareTo(o.getTotalWins());
        if (i != 0) {
            return i;
        }

        return username.compareTo(o.getUsername());
    }

    @JsonIgnore
    private String percentWin() {
        if (totalWins == 0 || attempts == 0)
            return "0 %";
        double percent = ((double) totalWins / (double) attempts) * 100;
        double twoDecimals = (double) Math.round(percent * 100) / 100;
        return twoDecimals + " %";
    }
}
