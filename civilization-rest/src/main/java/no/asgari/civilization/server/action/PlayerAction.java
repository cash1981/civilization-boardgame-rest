package no.asgari.civilization.server.action;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.exception.PlayerExistException;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Tradable;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

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

    /**
     * Returns a set of all the game ids of player
     */
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
    public GameLog chooseTech(String pbfId, ItemDTO item, String playerId) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.getName()); 

        //This can be done out of turn, because of EOI played in SOT
        //checkYourTurn(pbfId, playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        Optional<Tech> tech = pbf.getTechs().stream()
                .filter(techToFind -> techToFind.getName().equals(item.getName()))
                .findFirst();
                                           //if not static then this::cannotFindItem
        Tech chosenTech = tech.orElseThrow(PlayerAction::cannotFindItem);
        chosenTech.setHidden(true);
        chosenTech.setOwnerId(playerId);

        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        playerhand.getTechsChosen().add(chosenTech);

        pbfCollection.updateById(pbf.getId(), pbf);
        log.debug("Player " + playerId + " chose tech " + chosenTech.getName());

        return super.createLog(chosenTech, pbfId, GameLog.LogType.TECH);
    }

    public boolean endTurn(String pbfId, String username) {
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(username);

        PBF pbf = pbfCollection.findOneById(pbfId);

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

    /**
     * Revealing of items are really just saving a public log with the hidden content information
     * @param pbfId
     * @param playerId
     * @param gameLogId - The id of the GameLog which contains the item to reveal
     */
    @SuppressWarnings("unchecked")
    public void revealItem(String pbfId, String playerId, String gameLogId) {
        Preconditions.checkNotNull(gameLogId);
        Preconditions.checkNotNull(pbfId);
        Preconditions.checkNotNull(playerId);

        PBF pbf = pbfCollection.findOneById(pbfId);
        GameLog gameLog = logAction.findGameLogById(gameLogId);
        if(gameLog.getDraw() == null || gameLog.getDraw().getItem() == null) {
            log.error("Nothing to reveal");
            throw new WebApplicationException(Response.status(Response.Status.NOT_MODIFIED)
                    .entity("Nothing to reveal")
                    .build());
        }

        Item itemToReveal = gameLog.getDraw().getItem();

        /*Stream<Item> combinedItemStream = Stream.concat(playerhand.getItems().stream(), (Stream<Item>) playerhand.getTechsChosen());

        Item itemToReveal = combinedItemStream
                .filter(it -> it.getSheetName() == gameLogId.getSheetName())
                .filter(it -> it.getName().equals(gameLogId.getName()))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem);
        */

        itemToReveal.setHidden(false);
        logAction.createGameLog(itemToReveal, pbfId, GameLog.LogType.REVEAL);
        log.debug("item to be reveal " + itemToReveal);
        pbfCollection.updateById(pbfId, pbf);
    }

    /**
     * Returns the remaining techs the player can choose from
     * @param playerId - The player
     * @param pbfId - The PBF
     * @return
     */
    public List<Tech> getRemaingTechsForPlayer(String playerId, String pbfId) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        Set<Tech> techsChosen = pbf.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindPlayer)
                .getTechsChosen();


        pbf.getTechs().removeAll(techsChosen);
        return pbf.getTechs();
    }

    /**
     * Method that's checks whether it is players turn.
     * Not the same as #checkYourTurn()
     *
     * @see #checkYourTurn(String, String)
     * @param pbfId - PBF id
     * @param playerId - Player id
     * @return - true if it is players turn
     */
    public boolean isYourTurn(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);
        return playerhand.isYourTurn();
    }

    /**
     * Will send the item to the new owner
     * @param item
     * @param playerId
     * @return
     */
    public boolean tradeToPlayer(ItemDTO item, String playerId) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.getPbfId());
        Preconditions.checkNotNull(item.getOwnerId());

        PBF pbf = pbfCollection.findOneById(item.getPbfId());
        Playerhand fromPlayer = getPlayerhandByPlayerId(playerId, pbf);
        Playerhand toPlayer = getPlayerhandByPlayerId(item.getOwnerId(), pbf);

        Item itemToTrade = fromPlayer.getItems().stream()
                .filter(it -> it instanceof Tradable)
                .filter(it -> it.getSheetName() == item.getSheetName())
                .filter(it -> it.getName().equals(item.getName()))
                .findFirst()
                .orElseThrow(PlayerAction::cannotFindItem);

        boolean remove = fromPlayer.getItems().remove(itemToTrade);
        if(!remove) {
            log.error("Didn't find item from playerhand: " + item);
            return false;
        }
        toPlayer.getItems().add(itemToTrade);
        itemToTrade.setOwnerId(toPlayer.getPlayerId());
        pbfCollection.updateById(pbf.getId(), pbf);

        logAction.createGameLog(itemToTrade, pbf.getId(), GameLog.LogType.TRADE);
        return true;
    }

    public void discardItem(String pbfId, String playerId, ItemDTO itemdto) {
        PBF pbf = pbfCollection.findOneById(pbfId);

        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);

        Iterator<Item> iterator = playerhand.getItems().iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            if(item.getSheetName() == itemdto.getSheetName() && item.getName().equals(itemdto.getName())) {
                pbf.getDiscardedItems().add(item);
                iterator.remove();
                createLog(item, pbf.getId(), GameLog.LogType.DISCARD);
                pbfCollection.updateById(pbf.getId(), pbf);
                return;
            }
        }
        throw cannotFindItem();
    }
}
