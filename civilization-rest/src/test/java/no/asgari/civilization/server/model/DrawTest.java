package no.asgari.civilization.server.model;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

public class DrawTest extends AbstractMongoDBTest {

    @Test
    public void drawCivAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        SheetName CIV = SheetName.find("CIV").get();
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, CIV);
        
        assertThat(gameLogOptional.isPresent()).isTrue();
        GameLog gameLog = gameLogOptional.get();
        assertThat(gameLog.getDraw().getPlayerId()).isEqualToIgnoringCase(playerId);
        assertThat(gameLog.getDraw().getItem()).isExactlyInstanceOf(Civ.class);

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).doesNotContain(gameLog.getDraw().getItem());
    }

    @Test
    public void drawAircraftAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.AIRCRAFT);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Aircraft.class);
    }

    @Test
    public void drawArtilleryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Artillery.class);
    }

    @Test
    public void drawCitystateAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CITY_STATES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Citystate.class);
    }

    @Test
    public void drawCulture1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_1);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureI.class);
    }

    @Test
    public void drawCultureI1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_2);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureII.class);
    }

    @Test
    public void drawCulture3AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_3);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureIII.class);
    }

    @Test
    public void drawGPAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(GreatPerson.class);
    }

    @Test
    public void drawHutAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.HUTS);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Hut.class);
    }

    @Test
    public void drawInfantryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.INFANTRY);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Infantry.class);
    }

    @Test
    public void drawMountedAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.MOUNTED);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Mounted.class);
    }

    @Test
    public void drawTileAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.TILES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Tile.class);
    }

    @Test
    public void drawVillageAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.VILLAGES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Village.class);
    }

    @Test
    public void drawWonderAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.WONDERS);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts-1, pbfCollection.findOneById(pbfId).getItems().stream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Wonder.class);
    }

    @Test
    public void drawItemAndMakeSureLogsAreStored() throws Exception {
        long privateLog = gameLogCollection.count();
        DrawAction drawAction = new DrawAction(db);
        drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        assertThat(gameLogCollection.count()).isEqualTo(++privateLog);
    }
}
