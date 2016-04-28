package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinnerDTO implements Comparable<WinnerDTO> {
    private String username;
    private int totalWins;
    private long attempts;

    @Override
    public int compareTo(WinnerDTO o) {
        int i = Integer.valueOf(this.getTotalWins()).compareTo(o.getTotalWins());
        if(i != 0) {
            return i;
        }

        return username.compareTo(o.getUsername());
    }
}
