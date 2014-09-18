package no.asgari.civilization.server.excel;

import java.io.IOException;

import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;

public class PBFBuilder {

    /**
     * Will create a new game and read the content from the Excel sheet, and shuffle the content
     *
     * @return - A newly created PBF
     * @throws IOException
     */
    public PBF createNewGame() throws IOException {
        PBF pbf = new PBF();
        pbf.setNumOfPlayers(4); //TODO test only
        pbf.setName("First civ game"); //TODO test only
        pbf.setType(GameType.WAW);  //TODO test only

        ItemReader items = new ItemReader();
        items.readItemsFromExcel();

        UnitReader unit = new UnitReader();
        unit.readAllUnitsFromExcel();

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

        return pbf;
    }
}
