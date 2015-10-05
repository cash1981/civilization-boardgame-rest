package no.asgari.civilization.server.action;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.excel.ItemReader;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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

        if (CivSingleton.instance().itemsCache() == null) {
            CivSingleton.instance().setItemsCache(
                    CacheBuilder.newBuilder()
                            .maximumSize(4) //1 for each game type
                            .build(new CacheLoader<GameType, ItemReader>() {
                                public ItemReader load(GameType type) {
                                    ItemReader itemReader = new ItemReader();
                                    try {
                                        itemReader.readItemsFromExcel(GameType.WAW);
                                    } catch (IOException e) {
                                    }
                                    return itemReader;
                                }
                            })
            );
        }

        ItemReader items;
        try {
            items = CivSingleton.instance().itemsCache().get(GameType.WAW);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
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
        return pbf;
    }
}
