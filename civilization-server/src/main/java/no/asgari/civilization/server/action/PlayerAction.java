package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.BasicDBObject;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.Player;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.Optional;

@Log4j
public class PlayerAction {

    private final JacksonDBCollection<Player, String> playerCollection;

    public PlayerAction(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
    }

    /**
     * Returns the id to the player created
     * @param playerDTO - The DTO object
     * @return the id of the newly created player
     * @throws PlayerExistException - Throws this exception if username already exists
     */
    public String createPlayer(PlayerDTO playerDTO) throws PlayerExistException {
        Preconditions.checkNotNull(playerDTO);
        if(!playerDTO.getPassword().equals(playerDTO.getPasswordCopy())) {
            throw new IllegalArgumentException("Passwords are not identical");
        }
        if(findPlayerByUsername(playerDTO.getUsername()).isPresent()) {
            throw new PlayerExistException();
        }


        Player player = new Player();
        player.setUsername(playerDTO.getUsername());
        player.setPassword(playerDTO.getPassword());
        player.setEmail(playerDTO.getEmail());
        WriteResult<Player, String> insert = playerCollection.insert(player);

        log.info(String.format("Saving player with id %s", insert.getSavedId()));

        return insert.getSavedId();
    }

    public Optional<Player> findPlayerByUsername(String username) {
        @Cleanup DBCursor<Player> player = playerCollection.find(
                DBQuery.is("username", username),
                new BasicDBObject());

        if(player == null || !player.hasNext()) {
            return Optional.empty();
        }

        return Optional.of(player.next());
    }
}
