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
    private List<WinnerDTO> winners = new ArrayList<>();
    private List<WinnerDTO> fiveWinners = new ArrayList<>();
    private List<WinnerDTO> fourWinners = new ArrayList<>();
    private List<WinnerDTO> threeWinners = new ArrayList<>();
    private List<WinnerDTO> twoWinners = new ArrayList<>();
    private int totalNumberOfPlayers;
    private int totalNumberOfGames;
    private long fivePlayerGamesTotal;
    private long fourPlayerGamesTotal;
    private long threePlayerGamesTotal;
    private long twoPlayerGamesTotal;
}
