package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class CivHighscoreDTO {
    private List<CivWinnerDTO> winners = new ArrayList<>();
    private List<CivWinnerDTO> fiveWinners = new ArrayList<>();
    private List<CivWinnerDTO> fourWinners = new ArrayList<>();
    private List<CivWinnerDTO> threeWinners = new ArrayList<>();
    private List<CivWinnerDTO> twoWinners = new ArrayList<>();
    private int totalNumberOfPlayers;
    private int totalNumberOfGames;
}
