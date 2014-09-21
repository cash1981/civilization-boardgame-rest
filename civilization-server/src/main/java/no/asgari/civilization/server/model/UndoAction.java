package no.asgari.civilization.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * When player pushes revert or cancel a specific draw,
 * the system must find out how many players are in the game.
 * All those players must agree before a undo can be made
 *
 */
@Data
@NoArgsConstructor
@JsonRootName(value = "undoaction")
public class UndoAction {
    public static final String COL_NAME = "undoaction";

    @ObjectId
    @Id
    /** Will be used to identify a draw so that voting of undo can be performed **/
    private String id;

    /** draw_id that is going to be undoed **/
    @NotBlank
    private String drawId;

    /** If undo has been performed **/
    private boolean done;

    /** Although we can find this number each time, its easier to cache it here **/
    private int numberOfVotesRequired;

    /** Each player_id gets to vote **/
    private Map<String, Boolean> votes = new HashMap<>();

    public UndoAction(String drawId) {
        this.drawId = drawId;
        done = false;
        numberOfVotesRequired = 0;
    }

    @JsonIgnore
    public boolean hasAllVotedYes() {
        if(numberOfVotesRequired != votes.size()) {
            //Not everyone has voted yet
            return false;
        }

        return !votes.values().contains(false);
    }

}
