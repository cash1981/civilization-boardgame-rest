package no.asgari.civilization.server;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static no.asgari.civilization.server.SheetName.HUTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExcelTest {

    private InputStream in;
    private URL url;

    @Before
    public void checkThatFileExist() throws Exception {
        in = this.getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        url = this.getClass().getClassLoader().getResource("assets/gamedata-faf-waw.xlsx");

        assertNotNull(in);
        assertNotNull(url);
    }

    @After
    public void closeStream() throws Exception {
        in.close();
    }

    @Test
    public void checkThatBogusFileDoesNotExist() throws Exception {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("bogus.xlsx");
        URL url = this.getClass().getClassLoader().getResource("bogus.xlsx");

        assertNull(in);
        assertNull(url);
    }

    @Test
    public void iterateXLS() throws Exception {
        Workbook wb = new XSSFWorkbook(in);

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            System.out.println(wb.getSheetName(i));
            for (Row row : sheet) {
                System.out.println("rownum: " + row.getRowNum());
                for (Cell cell : row) {
                    System.out.println(cell.toString());
                }
            }
        }
    }

    @Test
    public void getHutSheet() throws Exception {
        Workbook wb = new XSSFWorkbook(in);
        Sheet sheet = wb.getSheet(HUTS.toString());
        assertNotNull(sheet);

        List<Cell> unfilteredHutCells = new ArrayList<>();

        sheet.forEach(row -> row.forEach(unfilteredHutCells::add)
        );

        assertFalse(unfilteredHutCells.isEmpty());

        List<String> huts = unfilteredHutCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(cell -> cell.getRow().getRowNum() != 0)
                .map(Object::toString)
                .collect(Collectors.toList());

        Collections.shuffle(huts);
        Queue<String> shuffledHuts = new LinkedList<>(huts);

        System.out.println(shuffledHuts);
        assertTrue(shuffledHuts.size() <= unfilteredHutCells.size());

        int sizeOfHuts = shuffledHuts.size();
        String hut = shuffledHuts.poll();
        System.out.println(hut);
        assertEquals(sizeOfHuts - 1, shuffledHuts.size());
    }

    public static <T> Stream<T> stream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static <T> Stream<T> parallelStream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), true);
    }
}
