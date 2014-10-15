package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * When player pushes revert or cancel a specific draw,
 * the system must find out how many players are in the game.
 * All those players must agree before a undo can be made
 *
 */
@Data
@NoArgsConstructor
@JsonRootName(value = "undo")
public class Undo {
    /** If undo has been performed **/
    private boolean done;

    /** Although we can find this number each time, its easier to cache it here **/
    private int numberOfVotesRequired;

    /** Each player_id gets to vote
     * The value can be true, false or null. Null means not voted yet **/
    private Map<String, Boolean> votes = new HashMap<>();

    public Undo(int numberOfVotesRequired) {
        this.numberOfVotesRequired = numberOfVotesRequired;
        done = false;
    }

    /**
     * Return the number of votes
     */
    @JsonIgnore
    public int numberOfVotesPerformed() {
        return votes.size();
    }

    @JsonIgnore
    public void vote(String playerId, Boolean vote) {
        votes.put(playerId, vote);
    }

    /**
     * Get the number of votes remaining
     */
    @JsonIgnore
    public int votesRemaining() {
        return Math.abs(votes.size() - numberOfVotesRequired);
    }

    /**
     * All must agree for draw to be performed.
     * If absent/empty, then all players have not voted
     */
    @JsonIgnore
    public Optional<Boolean> getResultOfVotes() {
        if(votesRemaining() != 0) return Optional.empty();

        if(getVotes().containsValue(Boolean.FALSE)) return Optional.of(Boolean.FALSE);


        return Optional.of(Boolean.TRUE);
    }


}
