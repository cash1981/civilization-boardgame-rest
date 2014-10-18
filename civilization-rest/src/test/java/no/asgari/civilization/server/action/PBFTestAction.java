package no.asgari.civilization.server.action;

import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;

import java.io.IOException;

public class PBFTestAction {

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

        pbf.getItems().addAll(items.mountedList);
        pbf.getItems().addAll(items.aircraftList);
        pbf.getItems().addAll(items.artilleryList);
        pbf.getItems().addAll(items.infantryList);

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
