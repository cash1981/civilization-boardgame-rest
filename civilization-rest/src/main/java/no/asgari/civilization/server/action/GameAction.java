package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Log4j
public class GameAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Player, String> playerCollection;

    public GameAction(DB db) {
        super(db);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public String createNewGame(CreateNewGameDTO dto) {
        PBF pbf = new PBF();
        pbf.setName(dto.getName());
        pbf.setType(dto.getType());
        pbf.setNumOfPlayers(dto.getNumOfPlayers());
        if (CivSingleton.instance().itemsCache() == null) {
            CivSingleton.instance().setItemsCache(
                    CacheBuilder.newBuilder()
                            .maximumSize(4) //1 for each game type
                            .removalListener(lis -> log.debug("Removing " + lis.getKey() + " from the gameCache"))
                            .build(new CacheLoader<GameType, ItemReader>() {
                                public ItemReader load(GameType type) {
                                    ItemReader itemReader = new ItemReader();
                                    try {
                                        itemReader.readItemsFromExcel(dto.getType());
                                    } catch (IOException e) {
                                        log.error("Couldn't read from Excel file " + e.getMessage() , e);
                                        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                                    }
                                    return itemReader;
                                }
                            })
            );
        }

        ItemReader itemReader;
        try {
            itemReader = CivSingleton.instance().itemsCache().get(dto.getType());
        } catch (ExecutionException e) {
            log.error("Couldnt get itemReader from cache " + e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        pbf.getItems().addAll(itemReader.shuffledCivs);
        pbf.getItems().addAll(itemReader.shuffledCultureI);
        pbf.getItems().addAll(itemReader.shuffledCultureII);
        pbf.getItems().addAll(itemReader.shuffledCultureIII);
        pbf.getItems().addAll(itemReader.shuffledGPs);
        pbf.getItems().addAll(itemReader.shuffledHuts);
        pbf.getItems().addAll(itemReader.shuffledVillages);
        pbf.getItems().addAll(itemReader.shuffledTiles);
        pbf.getItems().addAll(itemReader.shuffledCityStates);
        pbf.getItems().addAll(itemReader.ancientWonders);
        pbf.getItems().addAll(itemReader.medievalWonders);
        pbf.getItems().addAll(itemReader.modernWonders);
        pbf.getItems().addAll(itemReader.mountedList);
        pbf.getItems().addAll(itemReader.aircraftList);
        pbf.getItems().addAll(itemReader.artilleryList);
        pbf.getItems().addAll(itemReader.infantryList);
        pbf.getTechs().addAll(itemReader.allTechs);

        WriteResult<PBF, String> pbfInsert = pbfCollection.insert(pbf);
        pbf.setId(pbfInsert.getSavedId());
        log.info("PBF game created with id " + pbfInsert.getSavedId());

        DBCursor<Player> dBCursorPlayer = playerCollection.find(DBQuery.is("username", dto.getUsername()), new BasicDBObject());
        if(!dBCursorPlayer.hasNext()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        //TODO rewrite with update gameid statement. Then no need to find and update
        Player player = dBCursorPlayer.next();
        player.getGameIds().add(pbfInsert.getSavedId());
        playerCollection.updateById(player.getId(), player);
        log.debug("Added pbf to player");
        return pbf.getId();
    }

    public List<PbfDTO> getAllActiveGames() {
        @Cleanup DBCursor<PBF> dbCursor = pbfCollection.find(DBQuery.is("active", true), new BasicDBObject());

        List<PbfDTO> pbfs = new ArrayList<>();
        while (dbCursor.hasNext()) {
            PBF pbf = dbCursor.next();

            PbfDTO dto = new PbfDTO();
            dto.setType(pbf.getType());
            dto.setId(pbf.getId());
            dto.setName(pbf.getName());
            dto.setActive(pbf.isActive());
            dto.setCreated(pbf.getCreated());
            dto.setNumOfPlayers(pbf.getNumOfPlayers());
            dto.setPlayers(pbf.getPlayers().stream()
                    .map(this::createPlayerDTO)
                            //.map(p -> createPlayerDTO(p))
                    .collect(Collectors.toList()));
            pbfs.add(dto);
        }

        return pbfs;
    }

    private PlayerDTO createPlayerDTO(Playerhand player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setUsername(player.getUsername());
        dto.setPlayerId(player.getPlayerId());
        return dto;
    }

    public void joinGame(String pbfId, String username) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        if(pbf.getNumOfPlayers() == pbf.getPlayers().size()) {
            log.warn("Cannot join the game. Its full");
            Response badReq = Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot join the game. Its full.")
                    .build();
            throw new WebApplicationException(badReq);
        }

        DBCursor<Player> dbCursor = playerCollection.find(DBQuery.is("username", username), new BasicDBObject());
        if(!dbCursor.hasNext()) {
            log.error("Couldn't find dbCursor by username " + username);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        //TODO Better to use update instead of find & update
        Player player = dbCursor.next();
        player.getGameIds().add(pbfId);

        playerCollection.updateById(player.getId(), player);

        Playerhand playerhand = new Playerhand();
        playerhand.setPlayerId(player.getId());
        playerhand.setUsername(player.getUsername());

        if(!pbf.getPlayers().contains(playerhand)) {
            pbf.getPlayers().add(playerhand);
        }
        startIfAllPlayers(pbf);
        pbfCollection.updateById(pbf.getId(), pbf);

    }

    private void startIfAllPlayers(PBF pbf) {
        final int numOfPlayersNeeded = pbf.getNumOfPlayers();
        if(numOfPlayersNeeded == pbf.getPlayers().size()) {
            Playerhand randomPlayer = getRandomPlayer(pbf.getPlayers());
            log.debug("Setting starting player");
            randomPlayer.setYourTurn(true);
        }
    }

    private Playerhand getRandomPlayer(List<Playerhand> players) {
        Collections.shuffle(players);
        return players.get(0);
    }

    public List<PlayerDTO> getAllPlayers(String pbfId) {
        Preconditions.checkNotNull(pbfId);
        PBF pbf = pbfCollection.findOneById(pbfId);
        return pbf.getPlayers().stream()
                .map(p -> createPlayerDTO(p))
                .collect(Collectors.toList());
    }
}
