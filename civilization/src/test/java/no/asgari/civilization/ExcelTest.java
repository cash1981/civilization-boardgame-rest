package no.asgari.civilization;

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

import static no.asgari.civilization.ExcelSheet.Sheet.HUTS;
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
        in = this.getClass().getClassLoader().getResourceAsStream("gamedata-faf-waw.xlsx");
        url = this.getClass().getClassLoader().getResource("gamedata-faf-waw.xlsx");

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

        List<Cell> hutCells = new ArrayList<>();

        sheet.forEach(row -> row.forEach(cell -> hutCells.add(cell))
        );
        assertFalse(hutCells.isEmpty());

        List<String> huts = hutCells.stream()
                .filter(p -> !p.toString().equals("RAND()"))
                .map(c -> new String(c.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(huts);
        Queue<String> shuffledHuts = new LinkedList(huts);

        System.out.println(shuffledHuts);
        assertTrue(shuffledHuts.size() < hutCells.size());

        int sizeOfHuts = shuffledHuts.size();
        String hut = shuffledHuts.poll();
        System.out.println(hut);
        assertEquals(sizeOfHuts - 1, shuffledHuts.size());
    }


}
