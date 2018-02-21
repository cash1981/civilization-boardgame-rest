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
    public PBF createNewGame(String name) throws IOException {
        PBF pbf = new PBF();
        pbf.setNumOfPlayers(4);
        pbf.setName(name);
        pbf.setType(GameType.WAW);

        ItemReader items = new ItemReader();
        try {
            items.readItemsFromExcel(GameType.WAW);
        } catch (IOException e) {
        }

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
        pbf.getSocialPolicies().addAll(items.socialPolicies);

        pbf.getItems().forEach(it -> it.setItemNumber(ItemReader.itemCounter.incrementAndGet()));
        pbf.getTechs().forEach(it -> it.setItemNumber(ItemReader.itemCounter.incrementAndGet()));
        pbf.getSocialPolicies().forEach(it -> it.setItemNumber(ItemReader.itemCounter.incrementAndGet()));

        return pbf;
    }
}
