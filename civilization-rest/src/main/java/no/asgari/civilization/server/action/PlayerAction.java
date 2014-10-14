package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tech;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;

@Log4j
public class PlayerAction extends BaseAction {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public PlayerAction(DB db) {
        super(db);
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
        if(CivSingleton.instance().playerCache().asMap().containsValue(playerDTO.getUsername())) {
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

    public Set<String> getGames(Player player) {
        Preconditions.checkNotNull(player);
        log.debug("Getting all games for player " + player.getUsername());
        return player.getGameIds();
    }

    /**
     * Choose a tech for player and store back in the pbf collection
     * @param pbfId - The pbf id
     * @param item - The tech
     * @param playerId - The id of player
     */
    public void chooseTech(String pbfId, ItemDTO item, String playerId) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.getName());
        PBF pbf = getPBF(pbfId);
        Optional<Tech> tech = pbf.getTechs().stream()
                .filter(techToFind -> techToFind.getName().equals(item.getName()))
                .findFirst();
                                           //if not static then this::cannotFindItem
        Tech chosenTech = tech.orElseThrow(PlayerAction::cannotFindItem);
        Playerhand playerhand = getPlayerhandFromPlayerId(playerId, pbf);

        chosenTech.setHidden(true);
        chosenTech.setOwnerId(playerId);

        playerhand.getTechsChosen().add(chosenTech);

        pbfCollection.updateById(pbf.getId(), pbf);
        log.debug("Player " + playerId + " chose tech " + chosenTech.getName());

        super.createLog(chosenTech, pbfId);
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
                playerhand.setYourTurn(false);
                if(pbf.getPlayers().size() == (i+1)) {
                    //We are at the end, pick the first player
                    pbf.getPlayers().get(0).setYourTurn(true);
                }
                //Choose next player in line to be starting player
                pbf.getPlayers().get(i+1).setYourTurn(true);
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

    public boolean isYourTurn(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandFromPlayerId(playerId, pbf);
        return playerhand.isYourTurn();
    }

    private static Playerhand getPlayerhandFromPlayerId(String playerId, PBF pbf) {
        return pbf.getPlayers()
                .stream().filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem);
    }

    /**
     * Revealing of items are really just saving a public log with the hidden content information
     * @param pbfId
     * @param playerId
     * @param itemDTO
     */
    public void revealItem(String pbfId, String playerId, ItemDTO itemDTO) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        Item itemToReveal = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem)
                    .getItems().stream()
                    .filter(it -> it.getSheetName() == itemDTO.getSheetName())
                    .filter(it -> it.getName().equals(itemDTO.getName()))
                    .filter(it -> it.getType().equals(itemDTO.getType()))
                    .findFirst()
                    .orElseThrow(PlayerAction::cannotFindItem);


        itemToReveal.setHidden(false);
        log.debug("Setting item to reveal");
        pbfCollection.updateById(pbfId, pbf);

    }
}
