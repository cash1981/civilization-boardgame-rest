package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tech;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j
public class PlayerAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public PlayerAction(DB db) {
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    /**
     * Returns the id to the player created
     * @param playerDTO - The DTO object
     * @return the id of the newly created player
     * @throws PlayerExistException - Throws this exception if username already exists
     */
    public String createPlayer(PlayerDTO playerDTO) throws PlayerExistException {
        Preconditions.checkNotNull(playerDTO);
        Preconditions.checkNotNull(playerDTO.getUsername());
        Preconditions.checkNotNull(playerDTO.getEmail());
        Preconditions.checkNotNull(playerDTO.getPassword());
        Preconditions.checkNotNull(playerDTO.getPasswordCopy());

        if(!playerDTO.getPassword().equals(playerDTO.getPasswordCopy())) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                            .entity("Passwords are not identical")
                                            .build());
        }
        if(findPlayerByUsername(playerDTO.getUsername()).isPresent()) {
            throw new PlayerExistException();
        }

        Player player = new Player();
        player.setUsername(playerDTO.getUsername());
        player.setPassword(DigestUtils.sha1Hex(playerDTO.getPassword()));
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

    public List<PBF> getGames(Player player) {
        Preconditions.checkNotNull(player);
        log.debug("Getting all games for player " + player.getUsername());

        return player.getGameIds()
                .stream()
                .map(this::getPBF)
                .filter(PBF::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Choose a tech for player and store back in the pbf collection
     * @param pbfId - The pbf id
     * @param item - The tech
     * @param username - The username of player
     */
    public void chooseTech(String pbfId, ItemDTO item, String username) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.getName());
        PBF pbf = getPBF(pbfId);
        Optional<Tech> tech = pbf.getTechs().stream()
                .filter(techToFind -> techToFind.getName().equals(item.getName()))
                .findFirst();
                                           //if not static then this::cannotFindItem
        Tech chosenTech = tech.orElseThrow(PlayerAction::cannotFindItem);
        Playerhand playerhand = pbf.getPlayers()
                .stream().filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem);

        chosenTech.setHidden(true);
        chosenTech.setOwner(username);

        playerhand.getTechsChosen().add(chosenTech);

        pbfCollection.updateById(pbf.getId(), pbf);
        log.debug("Player " + username + " chose tech " + chosenTech.getName());

        //TODO private and public log
    }

    private static WebApplicationException cannotFindItem() {
        return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity("Could not find item")
                .build());
    }

    private PBF getPBF(String pbfId) {
        return pbfCollection.findOneById(pbfId);
    }

    public boolean endTurn(String pbfId, String username) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(username);

        PBF pbf = getPBF(pbfId);

        //Loop through the list and find next starting player
        for(int i = 0; i < pbf.getPlayers().size(); i++) {
            Playerhand playerhand = pbf.getPlayers().get(i);
            if(playerhand.getUsername().equals(username)) {
                playerhand.setStartingPlayer(false);
                if(pbf.getPlayers().size() == (i+1)) {
                    //We are at the end, pick the first player
                    pbf.getPlayers().get(0).setStartingPlayer(true);
                }
                //Choose next player in line to be starting player
                pbf.getPlayers().get(i+1).setStartingPlayer(true);
                try {
                    pbfCollection.updateById(pbf.getId(), pbf);
                    return true;
                } catch (Exception ex) {
                    log.error("Couldn't update pbf " + ex.getMessage(), ex);
                    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Couldn't update pbf")
                            .build());
                }
            }

        }
        return false;
    }
}
