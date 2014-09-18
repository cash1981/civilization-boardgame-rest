package no.asgari.civilization.server.model;

import no.asgari.civilization.server.excel.UnitReader;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class UnitReaderTest {

    @Test
    public void readUnitsFromExcel() throws IOException {
        UnitReader unitReader = new UnitReader();

        assertNull(unitReader.mountedList);
        assertNull(unitReader.artilleryList);
        assertNull(unitReader.infantryList);
        assertNull(unitReader.aircraftList);

        unitReader.readAllUnitsFromExcel();

        assertThat(unitReader.mountedList).isNotEmpty();
        assertThat(unitReader.artilleryList).isNotEmpty();
        assertThat(unitReader.infantryList).isNotEmpty();
        assertThat(unitReader.aircraftList).isNotEmpty();
    }
}
