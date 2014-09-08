package no.asgari.civilization.excel;

import no.asgari.civilization.ExcelSheet;
import no.asgari.civilization.representations.*;
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
import java.util.Queue;
import java.util.stream.Collectors;

public class ItemReader {
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

    public void readItemsFromExcel() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        Workbook wb = new XSSFWorkbook(in);

        civs = getShuffledCivsFromExcel(wb);

        shuffledCultureI = getShuffledCultureIFromExcel(wb);

        shuffledCultureII = getShuffledCultureIIFromExcel(wb);

        shuffledCultureIII = getShuffledCultureIIIFromExcel(wb);

        shuffledGPs = getShuffledGreatPersonFromExcel(wb);

        shuffledHuts = getShuffledHutsFromExcel(wb);

        shuffledVillages = getShuffledVillages(wb);

        getShuffledWonderFromExcel(wb);

        shuffledTiles = getShuffledTilesFromExcel(wb);
        wb.close();
    }

    private Queue<Item> getShuffledCivsFromExcel(Workbook wb) {
        Sheet civSheet = wb.getSheet(ExcelSheet.CIV.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        civSheet.forEach(row -> row.forEach(cell -> unfilteredCivCells.add(cell))
        );

        List<Civ> civs = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(civname -> new Civ(civname.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(civs);
        Queue<Item> shuffledCivs = new LinkedList<>(civs);

        return shuffledCivs;
    }

    private Queue<CultureI> getShuffledCultureIFromExcel(Workbook wb) {
        Sheet culture1Sheet = wb.getSheet(ExcelSheet.CULTURE_1.toString());

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
        for(int i = 0; i < cultures.size(); i++) {
            CultureI item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<CultureI> culture1 = new LinkedList<>(cultures);
        return culture1;
    }

    private Queue<CultureII> getShuffledCultureIIFromExcel(Workbook wb) {
        Sheet culture2Sheet = wb.getSheet(ExcelSheet.CULTURE_2.toString());

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
        for(int i = 0; i < cultures.size(); i++) {
            CultureII item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<CultureII> culture2 = new LinkedList<>(cultures);
        return culture2;
    }

    private Queue<CultureIII> getShuffledCultureIIIFromExcel(Workbook wb) {
        Sheet culture3Sheet = wb.getSheet(ExcelSheet.CULTURE_3.toString());

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
        for(int i = 0; i < cultures.size(); i++) {
            CultureIII item = cultures.get(i);
            item.setDescription(description.get(i));
        }

        Collections.shuffle(cultures);

        //Now we want to take every other one
        Queue<CultureIII> culture3 = new LinkedList<>(cultures);
        return culture3;
    }

    private Queue<GreatPerson> getShuffledGreatPersonFromExcel(Workbook wb) {
        Sheet gpSheet = wb.getSheet(ExcelSheet.GREAT_PERSON.toString());

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
        for(int i = 0; i < gps.size(); i++) {
            GreatPerson item = gps.get(i);
            item.setDescription(description.get(i));
            item.setType(tile.get(i));
        }

        Collections.shuffle(gps);

        //Now we want to take every other one
        Queue<GreatPerson> gpQueue = new LinkedList<>(gps);
        return gpQueue;
    }

    private void getShuffledWonderFromExcel(Workbook wb) {
        Sheet wonderSheet = wb.getSheet(ExcelSheet.WONDERS.toString());

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

    private Queue<Item> getShuffledTilesFromExcel(Workbook wb) {
        Sheet tileSheet = wb.getSheet(ExcelSheet.TILES.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        tileSheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );

        List<Tile> tiles = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(tilename -> new Tile(String.format("%d", (int) Double.valueOf(tilename.toString()).doubleValue())))
                .collect(Collectors.toList());

        Collections.shuffle(tiles);
        Queue<Item> shuffledTiles = new LinkedList<>(tiles);
        return shuffledTiles;
    }

    private Queue<Item> getShuffledHutsFromExcel(Workbook wb) {
        Sheet hutSheet = wb.getSheet(ExcelSheet.HUTS.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        hutSheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );

        List<Hut> huts = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(hut -> new Hut(hut.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(huts);
        Queue<Item> shuffledTiles = new LinkedList<>(huts);

        return shuffledTiles;
    }

    private Queue<Item> getShuffledVillages(Workbook wb) {
        Sheet sheet = wb.getSheet(ExcelSheet.VILLAGES.toString());

        List<Cell> unfilteredCivCells = new ArrayList<>();
        sheet.forEach(row -> row.forEach(unfilteredCivCells::add)
        );

        List<Village> villages = unfilteredCivCells.stream()
                .filter(p -> !p.toString().isEmpty())
                .filter(p -> !p.toString().equals("RAND()"))
                .filter(p -> p.getRow().getRowNum() != 0)
                .filter(cell -> cell.getColumnIndex() == 0)
                .map(village -> new Village(village.toString()))
                .collect(Collectors.toList());

        Collections.shuffle(villages);
        Queue<Item> shuffledTiles = new LinkedList<>(villages);

        return shuffledTiles;
    }

}
