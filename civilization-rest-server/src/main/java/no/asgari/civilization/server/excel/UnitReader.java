package no.asgari.civilization.server.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Aircraft;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Mounted;
import no.asgari.civilization.server.model.Unit;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Log4j
public class UnitReader {
    public LinkedList<Aircraft> aircraftList;
    public LinkedList<Artillery> artilleryList;
    public LinkedList<Mounted> mountedList;
    public LinkedList<Infantry> infantryList;

    public void readAllUnitsFromExcel() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("assets/gamedata-faf-waw.xlsx");
        try (Workbook wb = new XSSFWorkbook(in)) {
            createInfantryTest(wb);
            createMountedTest(wb);
            createArtilleryTest(wb);
            createAircraftTest(wb);
        }
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
