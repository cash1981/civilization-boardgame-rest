package no.asgari.civilization.server.model;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DrawTest extends AbstractMongoDBTest {

    @Test
    public void drawCivAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        SheetName CIV = SheetName.find("CIV").get();
        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, CIV);
        assertTrue(drawOptional.isPresent());

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        Draw draw = drawOptional.get();
        assertThat(draw.getItem()).isExactlyInstanceOf(Civ.class);
        assertThat(draw.getPbfId()).isEqualTo(pbfId);
        assertThat(pbf.getItems()).doesNotContain(draw.getItem());
    }

    @Test
    public void drawAircraftAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.AIRCRAFT);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Aircraft.class);
    }

    @Test
    public void drawArtilleryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Artillery.class);
    }

    @Test
    public void drawCitystateAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.CITY_STATES);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Citystate.class);
    }

    @Test
    public void drawCulture1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_1);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(CultureI.class);
    }

    @Test
    public void drawCultureI1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_2);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(CultureII.class);
    }

    @Test
    public void drawCulture3AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_3);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(CultureIII.class);
    }

    @Test
    public void drawGPAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(GreatPerson.class);
    }

    @Test
    public void drawHutAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.HUTS);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Hut.class);
    }

    @Test
    public void drawInfantryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.INFANTRY);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Infantry.class);
    }

    @Test
    public void drawMountedAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.MOUNTED);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Mounted.class);
    }

    @Test
    public void drawTileAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.TILES);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Tile.class);
    }

    @Test
    public void drawVillageAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.VILLAGES);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Village.class);
    }

    @Test
    public void drawWonderAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count();

        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.WONDERS);
        assertTrue(drawOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count());
        assertThat(drawOptional.get().getItem()).isExactlyInstanceOf(Wonder.class);
    }

    @Test
    public void drawItemAndMakeSureLogsAreStored() throws Exception {
        long publicLog = publicLogCollection.count();
        long privateLog = privateLogCollection.count();
        DrawAction drawAction = new DrawAction(db);
        drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        assertThat(publicLogCollection.count()).isEqualTo(++publicLog);
        assertThat(privateLogCollection.count()).isEqualTo(++privateLog);
    }


}
