package no.asgari.civilization.server.excel;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Aircraft;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Citystate;
import no.asgari.civilization.server.model.Civ;
import no.asgari.civilization.server.model.CultureI;
import no.asgari.civilization.server.model.CultureII;
import no.asgari.civilization.server.model.CultureIII;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.model.Hut;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.Mounted;
import no.asgari.civilization.server.model.Tech;
import no.asgari.civilization.server.model.Tile;
import no.asgari.civilization.server.model.Village;
import no.asgari.civilization.server.model.Wonder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Log4j
public class ItemReader {
    public LinkedList<Civ> shuffledCivs;
    public LinkedList<CultureI> shuffledCultureI;
    public LinkedList<CultureII> shuffledCultureII;
    public LinkedList<CultureIII> shuffledCultureIII;
    public LinkedList<GreatPerson> shuffledGPs;
    public LinkedList<Hut> shuffledHuts;
    public LinkedList<Village> shuffledVillages;
    public LinkedList<Wonder> modernWonders;
    public LinkedList<Wonder> medievalWonders;
    public LinkedList<Wonder> ancientWonders;
    public LinkedList<Tile> shuffledTiles;
    public LinkedList<Citystate> shuffledCityStates;
    public List<Tech> allTechs; //Not linked list because all players can choose the same tech

    public LinkedList<Aircraft> aircraftList;
    public LinkedList<Artillery> artilleryList;
    public LinkedList<Mounted> mountedList;
    public LinkedList<Infantry> infantryList;

    public ImmutableList<Item> redrawableItems;

    private static final Predicate<Cell> notEmptyPredicate = cell -> !cell.toString().isEmpty();
    private static final Predicate<Cell> notRandomPredicate = cell -> !cell.toString().equals("RAND()");
    private static final Predicate<Cell> rowNotZeroPredicate = cell -> cell.getRow().getRowNum() != 0;
    private static final Predicate<Cell> columnIndexZeroPredicate = cell -> cell.getColumnIndex() == 0;
    private static final String WONDERS = "wonders";

    @SuppressWarnings("unchecked")
    public void readItemsFromExcel(GameType gameType) throws IOException {
        InputStream in;
        switch (gameType) {
            case WAW:
                in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
                break;
            case FAF:
                throw new IOException("FAF not supported yet");
            case BASE:
                throw new IOException("Base is not supported yet");
            case DOC:
                throw new IOException("DoC is not supported yet");
            default:
                throw new IOException("For now we only support WAW");
        }

        try (Workbook wb = new XSSFWorkbook(in)) {
            shuffledCivs = (LinkedList<Civ>) getShuffledCivsFromExcel(wb);
            shuffledCultureI = (LinkedList<CultureI>) getShuffledCultureIFromExcel(wb);
            shuffledCultureII = (LinkedList<CultureII>) getShuffledCultureIIFromExcel(wb);
            shuffledCultureIII = (LinkedList<CultureIII>) getShuffledCultureIIIFromExcel(wb);
            shuffledGPs = (LinkedList<GreatPerson>) getShuffledGreatPersonFromExcel(wb);
            shuffledHuts = (LinkedList<Hut>) getShuffledHutsFromExcel(wb);
            shuffledVillages = (LinkedList<Village>) getShuffledVillages(wb);
            shuffledCityStates = (LinkedList<Citystate>) getShuffledCityStates(wb);
            shuffledTiles = (LinkedList<Tile>) getShuffledTilesFromExcel(wb);
            extractShuffledWondersFromExcel(wb);
            allTechs = getTechsFromExcel(wb);
            createInfantryTest(wb);
            createMountedTest(wb);
            createArtilleryTest(wb);
            createAircraftTest(wb);

            redrawableItems = ImmutableList.<Item>builder()
                    .addAll(shuffledCultureI)
                    .addAll(shuffledCultureII)
                    .addAll(shuffledCultureIII)
                    .addAll(infantryList)
                    .addAll(mountedList)
                    .addAll(artilleryList)
                    .addAll(aircraftList)
                    .addAll(shuffledGPs)
                    .build();
        }
    }

    private LinkedList<? extends Item> getShuffledCityStates(Workbook wb) {
        Sheet civSheet = wb.getSheet(SheetName.CITY_STATES.getName());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(unfilteredCivCells::add));

        List<Citystate> cityStates = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(civname -> new Citystate(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(cityStates);
        return new LinkedList<>(cityStates);
    }

    private LinkedList<? extends Item> getShuffledCivsFromExcel(Workbook wb) {
        Sheet civSheet = wb.getSheet(SheetName.CIV.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(unfilteredCivCells::add));

        List<Civ> civs = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(civname -> new Civ(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(civs);
        return new LinkedList<>(civs);
    }

    private LinkedList<? extends Item> getShuffledCultureIFromExcel(Workbook wb) {
        Sheet culture1Sheet = wb.getSheet(SheetName.CULTURE_1.getName());

        List<Cell> unfilteredCells = new ArrayList<>();
        culture1Sheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<CultureI> cultures = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new CultureI(cell.toString()))
                .collect(Collectors.toList());


        List<String> description = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        for (int i = 0; i < cultures.size(); i++) {
            CultureI item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);
        return new LinkedList<>(cultures);
    }

    private LinkedList<? extends Item> getShuffledCultureIIFromExcel(Workbook wb) {
        Sheet culture2Sheet = wb.getSheet(SheetName.CULTURE_2.getName());

        List<Cell> unfilteredCells = new ArrayList<>();
        culture2Sheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<CultureII> culture2s = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new CultureII(cell.toString()))
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as culture2s
        for (int i = 0; i < culture2s.size(); i++) {
            CultureII item = culture2s.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(culture2s);
        return new LinkedList<>(culture2s);
    }

    private LinkedList<? extends Item> getShuffledCultureIIIFromExcel(Workbook wb) {
        Sheet culture3Sheet = wb.getSheet(SheetName.CULTURE_3.getName());

        List<Cell> unfilteredCells = new ArrayList<>();
        culture3Sheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<CultureIII> culture3s = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new CultureIII(cell.toString()))
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as culture3s
        for (int i = 0; i < culture3s.size(); i++) {
            CultureIII item = culture3s.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(culture3s);
        return new LinkedList<>(culture3s);
    }

    private LinkedList<? extends Item> getShuffledGreatPersonFromExcel(Workbook wb) {
        Sheet gpSheet = wb.getSheet(SheetName.GREAT_PERSON.getName());

        List<Cell> unfilteredCells = new ArrayList<>();
        gpSheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<GreatPerson> gps = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new GreatPerson(cell.toString()))
                .collect(Collectors.toList());

        List<String> tile = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 2)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        for (int i = 0; i < gps.size(); i++) {
            GreatPerson item = gps.get(i);
            item.setDescription(description.get(i));
            item.setType(tile.get(i));
        }

        Collections.shuffle(gps);

        //Now we want to take every other one
        LinkedList<GreatPerson> gpLinkedList = new LinkedList<>(gps);
        return gpLinkedList;
    }

    private void extractShuffledWondersFromExcel(Workbook wb) {
        Sheet wonderSheet = wb.getSheet(SheetName.WONDERS.getName());

        List<Cell> unfilteredCells = new ArrayList<>();
        wonderSheet.forEach(row -> row.forEach(unfilteredCells::add));

        //Kategoriser wonderne

        List<String> wonderName = unfilteredCells.stream()
                .filter(p -> !p.toString().trim().isEmpty())
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.stream()
                .filter(p -> !p.toString().trim().isEmpty())
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        LinkedList<String> wondersName = new LinkedList<>(wonderName);
        LinkedList<String> descriptions = new LinkedList<>(description);

        //Kun ancient
        ancientWonders = new LinkedList<>();
        //There is no break in java 8 forEach, thus we use the old for
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = descriptions.poll();
            if (wonder.toLowerCase().contains(WONDERS)) {
                break;
            }
            ancientWonders.add(new Wonder(wonder, desc, Wonder.ANCIENT));
        }
        Collections.shuffle(ancientWonders);

        //Kun ancient
        medievalWonders = new LinkedList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = descriptions.poll();
            if (wonder.toLowerCase().contains(WONDERS)) {
                break;
            }
            medievalWonders.add(new Wonder(wonder, desc, Wonder.MEDIEVAL));
        }
        Collections.shuffle(medievalWonders);

        //Kun ancient
        modernWonders = new LinkedList<>();

        int remainingSize = wondersName.size();

        for (int i = 0; i < remainingSize; i++) {
            String wonder = wondersName.poll();
            String desc = descriptions.poll();
            modernWonders.add(new Wonder(wonder, desc, Wonder.MODERN));
        }
        Collections.shuffle(modernWonders);
    }

    private LinkedList<? extends Item> getShuffledTilesFromExcel(Workbook wb) {
        Sheet tileSheet = wb.getSheet(SheetName.TILES.getName());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        tileSheet.forEach(row -> row.forEach(unfilteredCivCells::add));

        List<Tile> tiles = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(tilename -> new Tile(String.format("%d", (int) Double.valueOf(tilename.toString()).doubleValue())))
                .collect(Collectors.toList());

        Collections.shuffle(tiles);
        return new LinkedList<>(tiles);
    }

    private LinkedList<? extends Item> getShuffledHutsFromExcel(Workbook wb) {
        Sheet hutSheet = wb.getSheet(SheetName.HUTS.getName());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        hutSheet.forEach(row -> row.forEach(unfilteredCivCells::add));

        List<Hut> huts = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(hut -> new Hut(hut.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(huts);
        return new LinkedList<>(huts);
    }

    private LinkedList<? extends Item> getShuffledVillages(Workbook wb) {
        Sheet sheet = wb.getSheet(SheetName.VILLAGES.getName());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(unfilteredCivCells::add));

        List<Village> villages = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(village -> new Village(village.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(villages);
        return new LinkedList<>(villages);
    }

    /**
     * Tech cards do not need to be shuffled as the player is supposed to pick the card they want
     *
     * @param wb
     * @return
     */
    private List<Tech> getTechsFromExcel(Workbook wb) {
        //Start with level 1 techs
        Sheet sheet = wb.getSheet(SheetName.LEVEL_1_TECH.getName());
        List<Cell> level1Cells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(level1Cells::add));

        List<Tech> level1Techs = level1Cells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(tech -> new Tech(tech.toString(), Tech.LEVEL_1))
                .collect(Collectors.toList());

        sheet = wb.getSheet(SheetName.LEVEL_2_TECH.getName());
        List<Cell> level2Cells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(level2Cells::add));

        List<Tech> level2Techs = level2Cells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(tech -> new Tech(tech.toString(), Tech.LEVEL_2))
                .collect(Collectors.toList());

        sheet = wb.getSheet(SheetName.LEVEL_3_TECH.getName());
        List<Cell> level3Cells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(level3Cells::add));

        List<Tech> level3Techs = level3Cells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(tech -> new Tech(tech.toString(), Tech.LEVEL_3))
                .collect(Collectors.toList());

        sheet = wb.getSheet(SheetName.LEVEL_4_TECH.getName());
        List<Cell> level4Cells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(level4Cells::add));

        List<Tech> level4Techs = level4Cells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(tech -> new Tech(tech.toString(), Tech.LEVEL_4))
                .collect(Collectors.toList());

        List<Tech> allTechs = new ArrayList<>(level1Techs);
        allTechs.addAll(level2Techs);
        allTechs.addAll(level3Techs);
        allTechs.addAll(level4Techs);
        allTechs.add(Tech.SPACE_FLIGHT);
        Collections.sort(allTechs, (o1, o2) -> Integer.valueOf(o1.getLevel()).compareTo(o2.getLevel()));

        return allTechs;
    }

    private void createInfantryTest(Workbook wb) throws IOException {
        Sheet infantrySheet = wb.getSheet(SheetName.INFANTRY.toString());


        List<Cell> unfilteredCells = new ArrayList<>();
        infantrySheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<Infantry> infantryUnits = unfilteredCells.stream()
                .filter(ItemReader.notEmptyPredicate)
                .filter(ItemReader.notRandomPredicate)
                .filter(ItemReader.rowNotZeroPredicate)
                .filter(ItemReader.columnIndexZeroPredicate)
                .map(cell -> createInfantry(cell.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(infantryUnits);
        infantryList = new LinkedList<>(infantryUnits);
        log.debug(infantryList);
    }

    private void createMountedTest(Workbook wb) throws IOException {
        Sheet mountedsheet = wb.getSheet(SheetName.MOUNTED.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        mountedsheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<Mounted> mountedUnits = unfilteredCells.stream()
                .filter(ItemReader.notEmptyPredicate)
                .filter(ItemReader.notRandomPredicate)
                .filter(ItemReader.rowNotZeroPredicate)
                .filter(ItemReader.columnIndexZeroPredicate)
                .map(cell -> createMounted(cell.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(mountedUnits);
        mountedList = new LinkedList<>(mountedUnits);
        log.debug("Mounted units from excel are " + mountedList);
    }

    private void createArtilleryTest(Workbook wb) throws IOException {
        Sheet artillerySheet = wb.getSheet(SheetName.ARTILLERY.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        artillerySheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<Artillery> artilleryUnits = unfilteredCells.stream()
                .filter(ItemReader.notEmptyPredicate)
                .filter(ItemReader.notRandomPredicate)
                .filter(ItemReader.rowNotZeroPredicate)
                .filter(ItemReader.columnIndexZeroPredicate)
                .map(cell -> createArtillery(cell.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(artilleryUnits);
        artilleryList = new LinkedList<>(artilleryUnits);
        log.debug("Artillery units from excel are " + artilleryList);
    }

    private void createAircraftTest(Workbook wb) throws IOException {
        Sheet aircraftSheet = wb.getSheet(SheetName.AIRCRAFT.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        aircraftSheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<Aircraft> aircraftUnits = unfilteredCells.stream()
                .filter(ItemReader.notEmptyPredicate)
                .filter(ItemReader.notRandomPredicate)
                .filter(ItemReader.rowNotZeroPredicate)
                .filter(ItemReader.columnIndexZeroPredicate)
                .map(cell -> createAircraft(cell.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(aircraftUnits);
        aircraftList = new LinkedList<>(aircraftUnits);
        log.debug("Aircraft units from excel are " + aircraftList);
    }

    private static Infantry createInfantry(String string) {
        Iterable<String> split = split(string);

        int attack = Integer.parseInt(Iterables.get(split, 0));
        int health = Integer.parseInt(Iterables.get(split, 1));

        return new Infantry(attack, health);
    }

    private static Artillery createArtillery(String string) {
        Iterable<String> split = split(string);
        int attack = Integer.parseInt(Iterables.get(split, 0));
        int health = Integer.parseInt(Iterables.get(split, 1));

        return new Artillery(attack, health);
    }

    private static Mounted createMounted(String string) {
        Iterable<String> split = split(string);
        int attack = Integer.parseInt(Iterables.get(split, 0));
        int health = Integer.parseInt(Iterables.get(split, 1));

        return new Mounted(attack, health);
    }

    private static Aircraft createAircraft(String string) {
        Iterable<String> split = split(string);
        int attack = Integer.parseInt(Iterables.get(split, 0));
        int health = Integer.parseInt(Iterables.get(split, 1));

        return new Aircraft(attack, health);
    }

    private static Iterable<String> split(String string) {
        return Splitter.onPattern(",|\\.").omitEmptyStrings().trimResults().split(string);
    }
}
