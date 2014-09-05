package no.asgari.civilization.representations;

import no.asgari.civilization.ExcelSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fest.util.Lists;
import org.junit.Test;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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

        Queue<? extends Item> civs = getShuffledCivsFromExcel(wb);
        assertNotNull(civs);

        Queue<? extends Item> shuffledCultureI = getShuffledCultureIFromExcel(wb);
        assertNotNull(shuffledCultureI);

        Queue<? extends Item> shuffledCultureII = getShuffledCultureIIFromExcel(wb);
        assertNotNull(shuffledCultureII);

        Queue<? extends Item> shuffledCultureIII = getShuffledCultureIIIFromExcel(wb);
        assertNotNull(shuffledCultureIII);

        Queue<? extends Item> shuffledGPs = getShuffledGreatPersonFromExcel(wb);
        assertNotNull(shuffledGPs);


        getShuffledWonderFromExcel(wb);
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

    private List<Wonder> getShuffledWonderFromExcel(Workbook wb) {
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
        List<Wonder> ancientWonders = new ArrayList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            System.out.println("Fjernet wonder " + wonder + " list har nå size " + wondersName.size());
            if (wonder.toLowerCase().contains("wonders")) {
                assertEquals("Description", desc);
                break;
            }

            ancientWonders.add(new Wonder(wonder, desc, "Ancient"));
        }

        assertEquals(9, ancientWonders.size());

        //Kun ancient
        List<Wonder> medivalWonder = new ArrayList<>();
        for (int i = 0; i < wondersName.size(); i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            System.out.println("Fjernet wonder " + wonder + " list har nå size " + wondersName.size());
            if (wonder.toLowerCase().contains("wonders")) {
                assertEquals("Description", desc);
                break;
            }
            medivalWonder.add(new Wonder(wonder, desc, "Medieval"));
        }

        assertEquals(9, medivalWonder.size());

        //Kun ancient
        List<Wonder> modernWonder = new ArrayList<>();

        int remainingSize = wondersName.size();

        for (int i = 0; i < remainingSize; i++) {
            String wonder = wondersName.poll();
            String desc = desciptions.poll();
            System.out.println("Fjernet wonder " + wonder + " list har nå size " + wondersName.size());
            modernWonder.add(new Wonder(wonder, desc, "Modern"));
        }

        assertEquals(9, modernWonder.size());

        List<Wonder> allWonders = new ArrayList<>(ancientWonders);
        allWonders.addAll(medivalWonder);
        allWonders.addAll(modernWonder);

        Collections.shuffle(allWonders);

        return allWonders;
    }

}
