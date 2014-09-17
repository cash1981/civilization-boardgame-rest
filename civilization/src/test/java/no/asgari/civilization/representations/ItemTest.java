package no.asgari.civilization.representations;

import no.asgari.civilization.ExcelSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ItemTest {
    public Queue<? extends Item> civs;
    public Queue<? extends Item> shuffledCultureI;
    public Queue<? extends Item> shuffledCultureII;
    public Queue<? extends Item> shuffledCultureIII;
    public Queue<? extends Item> shuffledGPs;
    public Queue<? extends Item> shuffledHuts;
    public Queue<? extends Item> shuffledVillages;
    public List<Wonder> modernWonder;
    public List<Wonder> medivalWonder;
    public List<Wonder> ancientWonders;
    public Queue<? extends Item> shuffledTiles;
    private Queue<? extends Item> shuffledCityStates;

    @Test
    public void readItemsFromExcel() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        Workbook wb = new XSSFWorkbook(in);

        civs = getShuffledCivsFromExcel(wb);
        assertNotNull(civs);

        shuffledCultureI = getShuffledCultureIFromExcel(wb);
        assertNotNull(shuffledCultureI);

        shuffledCultureII = getShuffledCultureIIFromExcel(wb);
        assertNotNull(shuffledCultureII);

        shuffledCultureIII = getShuffledCultureIIIFromExcel(wb);
        assertNotNull(shuffledCultureIII);

        shuffledGPs = getShuffledGreatPersonFromExcel(wb);
        assertNotNull(shuffledGPs);

        shuffledHuts = getShuffledHutsFromExcel(wb);
        assertNotNull(shuffledGPs);

        shuffledVillages = getShuffledVillages(wb);
        assertNotNull(shuffledGPs);

        getShuffledWonderFromExcel(wb);

        shuffledTiles = getShuffledTilesFromExcel(wb);

        shuffledCityStates = getShuffledCityStatesFromExcel(wb);
        assertNotNull(shuffledCityStates);

        wb.close();
    }

    private void assertQueue(Queue<? extends Item> queue, Collection<? extends Item> col) {
        assertTrue(queue.size() <= queue.size());

        int sizeOfHuts = queue.size();
        queue.poll(); //decrease size by 1
        assertEquals(sizeOfHuts - 1, queue.size());
    }

    private Queue<Item> getShuffledCivsFromExcel(Workbook wb) {
        Sheet civSheet = wb.getSheet(ExcelSheet.CIV.toString());
        assertNotNull(civSheet);

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(cell -> unfilteredCivCells.add(cell))
        );
        assertFalse(unfilteredCivCells.isEmpty());

        List<Civ> civs = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(civname -> new Civ(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(civs);
        Queue<Item> shuffledCivs = new LinkedList<>(civs);
        assertQueue(shuffledCivs, civs);

        System.out.println(shuffledCivs);
        return shuffledCivs;
    }

    private Queue<Item> getShuffledCityStatesFromExcel(Workbook wb) {
        Sheet civSheet = wb.getSheet(ExcelSheet.CITY_STATES.toString());
        assertNotNull(civSheet);

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(cell -> unfilteredCivCells.add(cell))
        );
        assertFalse(unfilteredCivCells.isEmpty());

        List<Citystate> cityStates = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(civname -> new Citystate(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(cityStates);
        Queue<Item> shuffled = new LinkedList<>(cityStates);
        assertQueue(shuffled, cityStates);

        System.out.println(shuffled);
        return shuffled;
    }

    private Queue<CultureI> getShuffledCultureIFromExcel(Workbook wb) {
        Sheet culture1Sheet = wb.getSheet(ExcelSheet.CULTURE_1.toString());
        assertNotNull(culture1Sheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        culture1Sheet.forEach(row -> row.forEach(unfilteredCells::add)
        );

        List<CultureI> cultures = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> new CultureI(cell.toString()))
                .collect(Collectors.toList());


        List<String> description = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        assertEquals(description.size(), cultures.size());
        for(int i = 0; i < cultures.size(); i++) {
            CultureI item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<CultureI> culture1 = new LinkedList<>(cultures);
        assertQueue(culture1, cultures);
        System.out.println(culture1);
        return culture1;
    }

    private Queue<CultureII> getShuffledCultureIIFromExcel(Workbook wb) {
        Sheet culture2Sheet = wb.getSheet(ExcelSheet.CULTURE_2.toString());
        assertNotNull(culture2Sheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        culture2Sheet.forEach(row -> row.forEach(unfilteredCells::add)
        );

        List<CultureII> cultures = unfilteredCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> new CultureII(cell.toString()))
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        assertEquals(description.size(), cultures.size());
        for(int i = 0; i < cultures.size(); i++) {
            CultureII item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<CultureII> culture2 = new LinkedList<>(cultures);
        assertQueue(culture2, cultures);
        System.out.println(culture2);
        return culture2;
    }

    private Queue<CultureIII> getShuffledCultureIIIFromExcel(Workbook wb) {
        Sheet culture3Sheet = wb.getSheet(ExcelSheet.CULTURE_3.toString());
        assertNotNull(culture3Sheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        culture3Sheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<CultureIII> cultures = unfilteredCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> new CultureIII(cell.toString()))
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        assertEquals(description.size(), cultures.size());
        for(int i = 0; i < cultures.size(); i++) {
            CultureIII item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<CultureIII> culture3 = new LinkedList<>(cultures);
        assertQueue(culture3, cultures);
        System.out.println(culture3);
        return culture3;
    }

    private Queue<GreatPerson> getShuffledGreatPersonFromExcel(Workbook wb) {
        Sheet gpSheet = wb.getSheet(ExcelSheet.GREAT_PERSON.toString());
        assertNotNull(gpSheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        gpSheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<GreatPerson> gps = unfilteredCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> new GreatPerson(cell.toString()))
                .collect(Collectors.toList());

        List<String> tile = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 2)
                .map(Object::toString)
                .collect(Collectors.toList());

        //Description should be in the same order as cultures
        assertEquals(description.size(), gps.size());
        assertEquals(tile.size(), gps.size());
        for(int i = 0; i < gps.size(); i++) {
            GreatPerson item = gps.get(i);
            item.setDescription(description.get(i));
            item.setType(tile.get(i));
        }

        Collections.shuffle(gps);

        //Now we want to take every other one
        Queue<GreatPerson> gpQueue = new LinkedList<>(gps);
        assertQueue(gpQueue, gps);
        System.out.println(gpQueue);
        return gpQueue;
    }

    private void getShuffledWonderFromExcel(Workbook wb) {
        Sheet wonderSheet = wb.getSheet(ExcelSheet.WONDERS.toString());
        assertNotNull(wonderSheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        wonderSheet.forEach(row -> row.forEach(unfilteredCells::add));

        //Kategoriser wonderne

        List<String> wonderName = unfilteredCells.stream()
                .filter(p -> !p.toString().trim().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> description = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().trim().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 1)
                .map(Object::toString)
                .collect(Collectors.toList());

        assertEquals(wonderName.size(), description.size());

        LinkedList<String> wondersName = new LinkedList<>(wonderName);
        LinkedList<String> desciptions = new LinkedList<>(description);

        //Kun ancient
        ancientWonders = new ArrayList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            System.out.println("Fjernet wonder " + wonder + " list har nå size " + wondersName.size());
            if (wonder.toLowerCase().contains("wonders")) {
                assertEquals("Description", desc);
                break;
            }

            ancientWonders.add(new Wonder(wonder, desc, Wonder.ANCIENT));
        }
        Collections.shuffle(ancientWonders);
        assertEquals(9, ancientWonders.size());

        //Kun ancient
        medivalWonder = new ArrayList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            System.out.println("Fjernet wonder " + wonder + " list har nå size " + wondersName.size());
            if (wonder.toLowerCase().contains("wonders")) {
                assertEquals("Description", desc);
                break;
            }
            medivalWonder.add(new Wonder(wonder, desc, Wonder.MEDIEVAL));
        }
        Collections.shuffle(medivalWonder);
        assertEquals(9, medivalWonder.size());

        //Kun ancient
        modernWonder = new ArrayList<>();

        int remainingSize = wondersName.size();

        for (int i = 0; i < remainingSize; i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            System.out.println("Fjernet wonder " + wonder + " list har nå size " + wondersName.size());
            modernWonder.add(new Wonder(wonder, desc, Wonder.MODERN));
        }
        Collections.shuffle(modernWonder);
        assertEquals(9, modernWonder.size());
    }

    private Queue<Item> getShuffledTilesFromExcel(Workbook wb) {
        Sheet tileSheet = wb.getSheet(ExcelSheet.TILES.toString());
        assertNotNull(tileSheet);

        List<Cell> unfilteredCivCells = new ArrayList<>();
        tileSheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );
        assertFalse(unfilteredCivCells.isEmpty());

        List<Tile> tiles = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(tilename -> new Tile(String.format("%d", (int) Double.valueOf(tilename.toString()).doubleValue())))
                .collect(Collectors.toList());

        Collections.shuffle(tiles);
        Queue<Item> shuffledTiles = new LinkedList<>(tiles);
        assertQueue(shuffledTiles, tiles);

        System.out.println(shuffledTiles);
        return shuffledTiles;
    }

    private Queue<Item> getShuffledHutsFromExcel(Workbook wb) {
        Sheet hutSheet = wb.getSheet(ExcelSheet.HUTS.toString());
        assertNotNull(hutSheet);

        List<Cell> unfilteredCivCells = new ArrayList<>();
        hutSheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );
        assertFalse(unfilteredCivCells.isEmpty());

        List<Hut> huts = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(hut -> new Hut(hut.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(huts);
        Queue<Item> shuffledTiles = new LinkedList<>(huts);
        assertQueue(shuffledTiles, huts);

        System.out.println(shuffledTiles);
        return shuffledTiles;
    }

    private Queue<Item> getShuffledVillages(Workbook wb) {
        Sheet sheet = wb.getSheet(ExcelSheet.VILLAGES.toString());
        assertNotNull(sheet);

        List<Cell> unfilteredCivCells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );
        assertFalse(unfilteredCivCells.isEmpty());

        List<Village> villages = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(village -> new Village(village.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(villages);
        Queue<Item> shuffledTiles = new LinkedList<>(villages);
        assertQueue(shuffledTiles, villages);

        System.out.println(shuffledTiles);
        return shuffledTiles;
    }

}
