package no.asgari.civilization.server.action;

import com.mongodb.BasicDBObject;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.excel.UnitReader;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Log4j
public class GameAction {

    private final JacksonDBCollection<PBF, String> pbfCollection;
    private final JacksonDBCollection<Player, String> playerCollection;

    public GameAction(JacksonDBCollection<PBF, String> pbfCollection, JacksonDBCollection<Player, String> playerCollection) {
        this.pbfCollection = pbfCollection;
        this.playerCollection = playerCollection;
    }

    public String createNewGame(CreateNewGameDTO dto) {
        PBF pbf = new PBF();
        pbf.setName(dto.getName());
        pbf.setType(dto.getType());
        pbf.setNumOfPlayers(dto.getNumOfPlayers());
        ItemReader items = new ItemReader();
        try {
            items.readItemsFromExcel();
        } catch (IOException e) {
            log.error("Couldn't read items from Excel file", e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        UnitReader unit = new UnitReader();
        try {
            unit.readAllUnitsFromExcel();
        } catch (IOException e) {
            log.error("Coludn't read units from Excel file", e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        pbf.setMounted(unit.mountedList);
        pbf.setAircraft(unit.aircraftList);
        pbf.setArtillery(unit.artilleryList);
        pbf.setInfantry(unit.infantryList);

        pbf.setCivs(items.shuffledCivs);
        pbf.setCultureIs(items.shuffledCultureI);
        pbf.setCultureIIs(items.shuffledCultureII);
        pbf.setCultureIIIs(items.shuffledCultureIII);
        pbf.setGreatPersons(items.shuffledGPs);
        pbf.setHuts(items.shuffledHuts);
        pbf.setVillages(items.shuffledVillages);
        pbf.setTiles(items.shuffledTiles);
        pbf.getCitystates().addAll(items.shuffledCityStates);
        pbf.getWonders().addAll(items.ancientWonders);
        pbf.getWonders().addAll(items.medievalWonders);
        pbf.getWonders().addAll(items.modernWonders);

        WriteResult<PBF, String> pbfInsert = pbfCollection.insert(pbf);
        pbf.setId(pbfInsert.getSavedId());
        log.info("PBF game craeted with id " + pbfInsert.getSavedId());

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
}
