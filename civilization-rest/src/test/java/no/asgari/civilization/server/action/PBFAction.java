package no.asgari.civilization.server.action;

import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.excel.UnitReader;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;

import java.io.IOException;

public class PBFAction {

    /**
     * Will create a new game and read the content from the Excel sheet, and shuffle the content
     *
     * @return - A newly created PBF
     * @throws IOException
     */
    public PBF createNewGame() throws IOException {
        PBF pbf = new PBF();
        pbf.setNumOfPlayers(4);
        pbf.setName("First civ game");
        pbf.setType(GameType.WAW);

        ItemReader items = new ItemReader();
        items.readItemsFromExcel(pbf.getType());

        UnitReader unit = new UnitReader();
        unit.readAllUnitsFromExcel();

        pbf.getItems().addAll(unit.mountedList);
        pbf.getItems().addAll(unit.aircraftList);
        pbf.getItems().addAll(unit.artilleryList);
        pbf.getItems().addAll(unit.infantryList);

        pbf.getItems().addAll(items.shuffledCivs);
        pbf.getItems().addAll(items.shuffledCultureI);
        pbf.getItems().addAll(items.shuffledCultureII);
        pbf.getItems().addAll(items.shuffledCultureIII);
        pbf.getItems().addAll(items.shuffledGPs);
        pbf.getItems().addAll(items.shuffledHuts);
        pbf.getItems().addAll(items.shuffledVillages);
        pbf.getItems().addAll(items.shuffledTiles);
        pbf.getItems().addAll(items.shuffledCityStates);
        pbf.getItems().addAll(items.ancientWonders);
        pbf.getItems().addAll(items.medievalWonders);
        pbf.getItems().addAll(items.modernWonders);
        pbf.getTechs().addAll(items.allTechs);
        return pbf;
    }
}
