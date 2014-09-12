package no.asgari.civilization.excel;

import com.google.common.collect.Lists;
import no.asgari.civilization.representations.GameType;
import no.asgari.civilization.representations.Item;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Player;

import java.util.ArrayList;
import java.util.List;

public class PBFTest {

    public PBF createGameTest() throws Exception {
        PBF pbf = GameBuilder.createPBF();
        List<Player> players = Lists.newArrayList(
                GameBuilder.createPlayer("cash1981"),
                GameBuilder.createPlayer("Itchi"),
                GameBuilder.createPlayer("Chul"),
                GameBuilder.createPlayer("Karandras1")
        );

        pbf.setPlayers(players);
        pbf.setNumOfPlayers(4);
        pbf.setName("First civ game");
        pbf.setType(GameType.WAW);


        ItemReader items = new ItemReader();
        items.readItemsFromExcel();

        UnitReader unit = new UnitReader();
        unit.readAllUnitsFromExcel();

        pbf.setMounted(new ArrayList<>(unit.mountedQueue));
        pbf.setAircraft(new ArrayList<>(unit.aircraftQueue));
        pbf.setArtillery(new ArrayList<>(unit.artilleryQueue));
        pbf.setInfantry(new ArrayList<>(unit.infantryQueue));

        List<Item> allItems = new ArrayList<>();
        allItems.addAll(items.shuffledCivs);
        allItems.addAll(items.shuffledCultureI);
        allItems.addAll(items.shuffledCultureII);
        allItems.addAll(items.shuffledCultureIII);
        allItems.addAll(items.shuffledGPs);
        allItems.addAll(items.shuffledHuts);
        allItems.addAll(items.shuffledVillages);
        allItems.addAll(items.shuffledTiles);
        allItems.addAll(items.ancientWonders);
        allItems.addAll(items.medivalWonder);
        allItems.addAll(items.modernWonder);

        pbf.setItems(allItems);

        return pbf;
    }

}
