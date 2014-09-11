package no.asgari.civilization.excel;

import com.google.common.collect.Lists;
import no.asgari.civilization.representations.*;

import java.util.ArrayList;
import java.util.List;

public class PBFTest {

    public PBF createGameTest() throws Exception {
        PBF pbf = GameBuilder.createPBF();
        List<Player> players = Lists.newArrayList(
                GameBuilder.createPlayer("cash1981"),
                GameBuilder.createPlayer("Itchi"),
                GameBuilder.createPlayer("DaveLuza"),
                GameBuilder.createPlayer("Karandras1")
        );

        pbf.setPlayers(players);
        pbf.setNumOfPlayers(4);
        pbf.setName("First civ game");


        ItemReader items = new ItemReader();
        items.readItemsFromExcel();

        UnitReader unit = new UnitReader();
        unit.readAllUnitsFromExcel();

        pbf.setMounted(new ArrayList<>(unit.mountedQueue));
        pbf.setAircraft(new ArrayList<>(unit.aircraftQueue));
        pbf.setArtillery(new ArrayList<>(unit.artilleryQueue));
        pbf.setInfantry(new ArrayList<>(unit.infantryQueue));

        List<Item> allItems = new ArrayList<>();
        pbf.setCivs((List<Civ>) items.shuffledCivs);
        pbf.setCultureIs((List<CultureI>) items.shuffledCultureI);
        pbf.setCultureIIs((List<CultureII>) items.shuffledCultureII);
        pbf.setCultureIIIs((List<CultureIII>) items.shuffledCultureIII);
        pbf.setGreatPersons((List<GreatPerson>) items.shuffledGPs);
        pbf.setHuts((List<Hut>) items.shuffledHuts);
        pbf.setVillages((List<Village>) items.shuffledVillages);
        pbf.setTiles((List<Tile>) items.shuffledTiles);
        pbf.getCitystates().addAll((List<Citystate>) items.shuffledCityStates);
        pbf.getWonders().addAll(items.ancientWonders);
        pbf.getWonders().addAll(items.medivalWonder);
        pbf.getWonders().addAll(items.modernWonder);

        return pbf;
    }

}
