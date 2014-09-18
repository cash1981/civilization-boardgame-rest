package no.asgari.civilization.server.excel;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Citystate;
import no.asgari.civilization.server.model.Civ;
import no.asgari.civilization.server.model.CultureI;
import no.asgari.civilization.server.model.CultureII;
import no.asgari.civilization.server.model.CultureIII;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.model.Hut;
import no.asgari.civilization.server.model.Item;
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

public class ItemReader {
    public LinkedList<Civ> shuffledCivs;
    public LinkedList<CultureI> shuffledCultureI;
    public LinkedList<CultureII> shuffledCultureII;
    public LinkedList<CultureIII> shuffledCultureIII;
    public LinkedList<GreatPerson> shuffledGPs;
    public LinkedList<Hut> shuffledHuts;
    public LinkedList<Village> shuffledVillages;
    public List<Wonder> modernWonder;
    public List<Wonder> medivalWonder;
    public List<Wonder> ancientWonders;
    public LinkedList<Tile> shuffledTiles;
    public LinkedList<Citystate> shuffledCityStates;
    
    private final Predicate<Cell> notEmptyPredicate = p -> !p.toString().isEmpty();
    private final Predicate<Cell> notRandomPredicate = p -> !p.toString().equals("RAND()");
    private final Predicate<Cell> rowNotZeroPredicate = p -> p.getRow().getRowNum() != 0;
    private final Predicate<Cell> columnIndexZeroPredicate = cell -> cell.getColumnIndex() == 0;

    @SuppressWarnings("unchecked")
    public void readItemsFromExcel() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        Workbook wb = new XSSFWorkbook(in);

        shuffledCivs = (LinkedList<Civ>) getShuffledCivsFromExcel(wb);

        shuffledCultureI = (LinkedList<CultureI>) getShuffledCultureIFromExcel(wb);

        shuffledCultureII = (LinkedList<CultureII>) getShuffledCultureIIFromExcel(wb);

        shuffledCultureIII = (LinkedList<CultureIII>) getShuffledCultureIIIFromExcel(wb);

        shuffledGPs = (LinkedList<GreatPerson>) getShuffledGreatPersonFromExcel(wb);

        shuffledHuts = (LinkedList<Hut>) getShuffledHutsFromExcel(wb);

        shuffledVillages = (LinkedList<Village>) getShuffledVillages(wb);

        shuffledCityStates = (LinkedList<Citystate>) getShuffledCityStates(wb);

        extractShuffledWonderFromExcel(wb);

        shuffledTiles = (LinkedList<Tile>) getShuffledTilesFromExcel(wb);
        wb.close();
    }

    private LinkedList<? extends Item> getShuffledCityStates(Workbook wb) {
        Sheet civSheet = wb.getSheet(SheetName.CITY_STATES.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(cell -> unfilteredCivCells.add(cell))
        );


        List<Citystate> cityStates = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(civname -> new Citystate(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(cityStates);
        LinkedList<Item> shuffled = new LinkedList<>(cityStates);
        return shuffled;
    }

    private LinkedList<? extends Item> getShuffledCivsFromExcel(Workbook wb) {
        Sheet civSheet = wb.getSheet(SheetName.CIV.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(cell -> unfilteredCivCells.add(cell))
        );

        List<Civ> civs = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(civname -> new Civ(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(civs);
        LinkedList<Civ> shuffledCivs = new LinkedList<>(civs);

        return shuffledCivs;
    }

    private LinkedList<? extends Item> getShuffledCultureIFromExcel(Workbook wb) {
        Sheet culture1Sheet = wb.getSheet(SheetName.CULTURE_1.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        culture1Sheet.forEach(row -> row.forEach(unfilteredCells::add)
        );

        List<CultureI> cultures = unfilteredCells.parallelStream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new CultureI(cell.toString()))
                .collect(Collectors.toList());


        List<String> description = unfilteredCells.parallelStream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        for(int i = 0; i < cultures.size(); i++) {
            CultureI item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        LinkedList<CultureI> culture1 = new LinkedList<>(cultures);
        return culture1;
    }

    private LinkedList<? extends Item> getShuffledCultureIIFromExcel(Workbook wb) {
        Sheet culture2Sheet = wb.getSheet(SheetName.CULTURE_2.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        culture2Sheet.forEach(row -> row.forEach(unfilteredCells::add)
        );

        List<CultureII> cultures = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new CultureII(cell.toString()))
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        for(int i = 0; i < cultures.size(); i++) {
            CultureII item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        LinkedList<CultureII> culture2 = new LinkedList<>(cultures);
        return culture2;
    }

    private LinkedList<? extends Item> getShuffledCultureIIIFromExcel(Workbook wb) {
        Sheet culture3Sheet = wb.getSheet(SheetName.CULTURE_3.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        culture3Sheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<CultureIII> cultures = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new CultureIII(cell.toString()))
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        for(int i = 0; i < cultures.size(); i++) {
            CultureIII item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        LinkedList<CultureIII> culture3 = new LinkedList<>(cultures);
        return culture3;
    }

    private LinkedList<? extends Item> getShuffledGreatPersonFromExcel(Workbook wb) {
        Sheet gpSheet = wb.getSheet(SheetName.GREAT_PERSON.toString());

        List<Cell> unfilteredCells = new ArrayList<>();
        gpSheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<GreatPerson> gps = unfilteredCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(cell -> new GreatPerson(cell.toString()))
                .collect(Collectors.toList());

        List<String> tile = unfilteredCells.parallelStream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 2)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        for(int i = 0; i < gps.size(); i++) {
            GreatPerson item = gps.get(i);
            item.setDescription(description.get(i));
            item.setType(tile.get(i));
        }

        Collections.shuffle(gps);

        //Now we want to take every other one
        LinkedList<GreatPerson> gpLinkedList = new LinkedList<>(gps);
        return gpLinkedList;
    }

    private void extractShuffledWonderFromExcel(Workbook wb) {
        Sheet wonderSheet = wb.getSheet(SheetName.WONDERS.toString());

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

        List<String> description = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().trim().isEmpty())
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        LinkedList<String> wondersName = new LinkedList<>(wonderName);
        LinkedList<String> desciptions = new LinkedList<>(description);

        //Kun ancient
        ancientWonders = new ArrayList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            if (wonder.toLowerCase().contains("wonders")) {
                break;
            }

            ancientWonders.add(new Wonder(wonder, desc, Wonder.ANCIENT));
        }
        Collections.shuffle(ancientWonders);

        //Kun ancient
        medivalWonder = new ArrayList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            if (wonder.toLowerCase().contains("wonders")) {
                break;
            }
            medivalWonder.add(new Wonder(wonder, desc, Wonder.MEDIEVAL));
        }
        Collections.shuffle(medivalWonder);

        //Kun ancient
        modernWonder = new ArrayList<>();

        int remainingSize = wondersName.size();

        for (int i = 0; i < remainingSize; i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            modernWonder.add(new Wonder(wonder, desc, Wonder.MODERN));
        }
        Collections.shuffle(modernWonder);
    }

    private LinkedList<? extends Item> getShuffledTilesFromExcel(Workbook wb) {
        Sheet tileSheet = wb.getSheet(SheetName.TILES.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        tileSheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );

        List<Tile> tiles = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(tilename -> new Tile(String.format("%d", (int) Double.valueOf(tilename.toString()).doubleValue())))
                .collect(Collectors.toList());

        Collections.shuffle(tiles);
        LinkedList<Tile> shuffledTiles = new LinkedList<>(tiles);
        return shuffledTiles;
    }

    private LinkedList<? extends Item> getShuffledHutsFromExcel(Workbook wb) {
        Sheet hutSheet = wb.getSheet(SheetName.HUTS.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        hutSheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );

        List<Hut> huts = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(hut -> new Hut(hut.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(huts);
        LinkedList<Hut> shuffledHuts = new LinkedList<>(huts);

        return shuffledHuts;
    }

    private LinkedList<? extends Item> getShuffledVillages(Workbook wb) {
        Sheet sheet = wb.getSheet(SheetName.VILLAGES.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );

        List<Village> villages = unfilteredCivCells.stream()
                .filter(notEmptyPredicate)
                .filter(notRandomPredicate)
                .filter(rowNotZeroPredicate)
                .filter(columnIndexZeroPredicate)
                .map(village -> new Village(village.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(villages);
        LinkedList<Village> shuffledVillages = new LinkedList<>(villages);

        return shuffledVillages;
    }
}
