package no.asgari.civilization.representations;

import no.asgari.civilization.ExcelSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fest.util.Lists;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PBFTest {

    /**
     * This test will create the setup with all items
     * @throws Exception
     */
    @Test
    public void createGameTest() throws Exception {
        PBF PBF = GameBuilder.createGame("1");
        List<Player> players = Lists.newArrayList(
            GameBuilder.createPlayer("1", "cash1981"),
            GameBuilder.createPlayer("2", "Itchi"),
            GameBuilder.createPlayer("3", "DaveLuza"),
            GameBuilder.createPlayer("4", "Karandras1")
        );
        InputStream in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        Workbook wb = new XSSFWorkbook(in);

        Queue<Item> civs = getShuffledCivsFromExcel(wb);
        assertNotNull(civs);

        Queue<Item> shuffledCultureI = getShuffledCultureIFromExcel(wb);
        assertNotNull(shuffledCultureI);

        Queue<Item> shuffledCultureII = getShuffledCultureIIFromExcel(wb);
        assertNotNull(shuffledCultureII);

        Queue<Item> shuffledCultureIII = getShuffledCultureIIIFromExcel(wb);
        assertNotNull(shuffledCultureIII);

        Queue<Item> shuffledGPs = getShuffledGreatPersonFromExcel(wb);
        assertNotNull(shuffledGPs);

        wb.close();
    }

    private void assertQueue(Queue<Item> queue, Collection<Item> col) {
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

        List<Item> civs = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(civname -> createItem(ExcelSheet.CIV, civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(civs);
        Queue<Item> shuffledCivs = new LinkedList<>(civs);
        assertQueue(shuffledCivs, civs);

        System.out.println(shuffledCivs);
        return shuffledCivs;
    }

    private Queue<Item> getShuffledCultureIFromExcel(Workbook wb) {
        Sheet culture1Sheet = wb.getSheet(ExcelSheet.CULTURE_1.toString());
        assertNotNull(culture1Sheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        culture1Sheet.forEach(row -> row.forEach(unfilteredCells::add)
        );

        List<Item> cultures = unfilteredCells.parallelStream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> createItem(ExcelSheet.CULTURE_1, cell.toString()))
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
            Item item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<Item> culture1 = new LinkedList<>(cultures);
        assertQueue(culture1, cultures);
        System.out.println(culture1);
        return culture1;
    }

    private Queue<Item> getShuffledCultureIIFromExcel(Workbook wb) {
        Sheet culture2Sheet = wb.getSheet(ExcelSheet.CULTURE_2.toString());
        assertNotNull(culture2Sheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        culture2Sheet.forEach(row -> row.forEach(unfilteredCells::add)
        );

        List<Item> cultures = unfilteredCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> createItem(ExcelSheet.CULTURE_2, cell.toString()))
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
            Item item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<Item> culture2 = new LinkedList<>(cultures);
        assertQueue(culture2, cultures);
        System.out.println(culture2);
        return culture2;
    }

    private Queue<Item> getShuffledCultureIIIFromExcel(Workbook wb) {
        Sheet culture3Sheet = wb.getSheet(ExcelSheet.CULTURE_3.toString());
        assertNotNull(culture3Sheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        culture3Sheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<Item> cultures = unfilteredCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> createItem(ExcelSheet.CULTURE_3, cell.toString()))
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
            Item item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<Item> culture3 = new LinkedList<>(cultures);
        assertQueue(culture3, cultures);
        System.out.println(culture3);
        return culture3;
    }

    private Queue<Item> getShuffledGreatPersonFromExcel(Workbook wb) {
        Sheet gpSheet = wb.getSheet(ExcelSheet.GREAT_PERSON.toString());
        assertNotNull(gpSheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        gpSheet.forEach(row -> row.forEach(unfilteredCells::add));

        List<Item> gps = unfilteredCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(cell -> createItem(ExcelSheet.GREAT_PERSON, cell.toString()))
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
            Item item = gps.get(i);
            item.setDescription(description.get(i));
            item.setType(tile.get(i));
        }

        Collections.shuffle(gps);

        //Now we want to take every other one
        Queue<Item> gpQueue = new LinkedList<>(gps);
        assertQueue(gpQueue, gps);
        System.out.println(gpQueue);
        return gpQueue;
    }

    private static Item createItem(ExcelSheet excelSheet, String name) {
        return new Item(excelSheet, name);
    }

}
