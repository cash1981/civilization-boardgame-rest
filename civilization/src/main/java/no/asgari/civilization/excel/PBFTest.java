package no.asgari.civilization.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.asgari.civilization.representations.Citystate;
import no.asgari.civilization.representations.Civ;
import no.asgari.civilization.representations.CultureI;
import no.asgari.civilization.representations.CultureII;
import no.asgari.civilization.representations.CultureIII;
import no.asgari.civilization.representations.GreatPerson;
import no.asgari.civilization.representations.Hut;
import no.asgari.civilization.representations.Item;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.representations.Tile;
import no.asgari.civilization.representations.Village;

public class PBFTest {

    public PBF createGameTest() throws IOException {
        PBF pbf = GameBuilder.createPBF();

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
