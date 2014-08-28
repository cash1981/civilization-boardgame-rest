package no.asgari.civilization.representations;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import no.asgari.civilization.ExcelSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UnitTest {

    public void createInfantryTest() throws Exception{
        InputStream in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        Workbook wb = new XSSFWorkbook(in);
        Sheet gpSheet = wb.getSheet(ExcelSheet.INFANTRY.toString());
        assertNotNull(gpSheet);

        List<Cell> unfilteredCells = new ArrayList<>();
        gpSheet.forEach(row -> row.forEach(unfilteredCells::add));

//        List<Infantry> units = unfilteredCells.stream()
//                .filter(p -> !p.toString().isEmpty())
//                .filter(p -> !p.toString().equals("RAND()"))
//                .filter(p -> p.getRow().getRowNum() != 0)
//                .filter(cell -> cell.getColumnIndex() == 0)
//                .map(cell -> new Infantry(cell.toString()))
//                .collect(Collectors.toList());

//        unfilteredCells.stream()
//                .filter(p -> !p.toString().isEmpty())
//                .filter(p -> !p.toString().equals("RAND()"))
//                .filter(p -> p.getRow().getRowNum() != 0)
//                .filter(cell -> cell.getColumnIndex() == 0)
////                .map(cell -> new Infantry(cell.toString()))
//                .collect(Collectors.toMap(cell.));
//
//
//        Collections.shuffle(gps);
//
//        //Now we want to take every other one
//        Queue<Infantry> gpQueue = new LinkedList<>(gps);
//        System.out.println(gpQueue);

        wb.close();
    }

    @Test
    public void splitStringTest() {
        ImmutableList<String> strings = ImmutableList.of("1,3", "1,3", "1,3", "1,3", "1,3", "3,1", "3,1", "3,1", "3,1", "3,1", "3,1", "2,2", "2,2", "2,2", "2,2", "2,2", "2,2", "2,2");
        List<Infantry> infantries = strings.stream()
                .map(val -> createInfantry(val))
                .collect(Collectors.toList());

        System.out.println(infantries);
    }

    private static Infantry createInfantry(String string) {
        Iterable<String> split = Splitter.onPattern(",|\\.").omitEmptyStrings().trimResults().split(string);
        assertEquals(2, Iterables.size(split));

        int attack = Integer.parseInt(Iterables.get(split, 0));
        int health = Integer.parseInt(Iterables.get(split, 1));

        return new Infantry(attack, health);
    }

    public static <T> Stream<T> stream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static <T> Stream<T> parallelStream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), true);
    }

}
