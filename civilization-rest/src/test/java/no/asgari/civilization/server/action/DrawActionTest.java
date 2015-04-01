package no.asgari.civilization.server.action;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.exception.NoMoreItemsException;
import no.asgari.civilization.server.model.Aircraft;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Citystate;
import no.asgari.civilization.server.model.Civ;
import no.asgari.civilization.server.model.CultureI;
import no.asgari.civilization.server.model.CultureII;
import no.asgari.civilization.server.model.CultureIII;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.model.Hut;
import no.asgari.civilization.server.model.Infantry;
import no.asgari.civilization.server.model.Mounted;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Tile;
import no.asgari.civilization.server.model.Unit;
import no.asgari.civilization.server.model.Village;
import no.asgari.civilization.server.model.Wonder;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DrawActionTest extends AbstractCivilizationTest {

    @Test
    public void drawCivAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        SheetName CIV = SheetName.find("CIV").get();
        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, CIV);

        assertThat(gameLogOptional.isPresent()).isTrue();
        GameLog gameLog = gameLogOptional.get();
        assertThat(gameLog.getDraw().getPlayerId()).isEqualToIgnoringCase(getApp().playerId);
        assertThat(gameLog.getDraw().getItem()).isExactlyInstanceOf(Civ.class);
        assertThat(gameLog.getPrivateLog()).matches(".+drew.*Civ.+");
        assertFalse(gameLog.getPublicLog().matches(".+drew.*Civ.+"));

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).doesNotContain(gameLog.getDraw().getItem());
    }

    @Test
    public void drawAircraftAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.AIRCRAFT);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Aircraft.class);
    }

    @Test
    public void drawArtilleryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.ARTILLERY);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Artillery.class);

        Artillery artillery = (Artillery) gameLogOptional.get().getDraw().getItem();
        assertThat(artillery.getImage()).doesNotContain(" ");
        assertThat(artillery.getImage()).endsWith(".png");
    }

    @Test
    public void drawCitystateAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.CITY_STATES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Citystate.class);
    }

    @Test
    public void drawCulture1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.CULTURE_1);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureI.class);
    }

    @Test
    public void drawCulture2AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.CULTURE_2);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureII.class);
    }

    @Test
    public void drawCulture3AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.CULTURE_3);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureIII.class);
    }

    @Test
    public void drawGPAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.GREAT_PERSON);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(GreatPerson.class);
    }

    @Test
    public void drawHutAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long hut = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.HUTS);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(hut - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Hut.class);
    }

    @Test
    public void drawInfantryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long infantries = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.INFANTRY);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(infantries - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Infantry.class);
    }

    @Test
    public void drawMountedAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.MOUNTED);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Mounted.class);
    }

    @Test
    public void drawTileAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.TILES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Tile.class);
    }

    @Test
    public void drawVillageAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.VILLAGES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Village.class);
    }

    @Test
    public void drawWonderAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.WONDERS);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Wonder.class);
    }

    @Test
    public void drawItemAndMakeSureLogsAreStored() throws Exception {
        long privateLog = getApp().gameLogCollection.count();
        DrawAction drawAction = new DrawAction(getApp().db);
        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.GREAT_PERSON);
        assertThat(getApp().gameLogCollection.count()).isEqualTo(++privateLog);
    }

    @Test(expected = NoMoreItemsException.class)
    public void makeSureSystemCorrectlyThrowsExceptionWhenNothingToShuffle() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);
        //Before draw
        long aircrafts = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        for (int i = 0; i < aircrafts; i++) {
            Optional<GameLog> draw = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.AIRCRAFT);
            assertThat(draw.isPresent());
        }

        long newCount = getApp().pbfCollection.findOneById(getApp().pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        assertThat(newCount).isEqualTo(0L);

        //Now if draw again, then it should throw exception since all units are in play
        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.AIRCRAFT);
    }

    @Test
    public void drawAndDiscardBarbarians() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertThat(playerhand.getBarbarians().isEmpty());

        List<Unit> units = drawAction.drawBarbarians(getApp().pbfId, getApp().playerId);
        assertThat(units).hasSize(3);

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertThat(playerhand.getBarbarians()).hasSize(3);

        //Finally discard the barbs
        drawAction.discardBarbarians(getApp().pbfId, getApp().playerId);

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertThat(playerhand.getBarbarians()).isEmpty();
    }

    @Test
    public void drawUnitForBattle() throws Exception {
        DrawAction drawAction = new DrawAction(getApp().db);

        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.INFANTRY);
        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.ARTILLERY);
        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.ARTILLERY);
        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.MOUNTED);
        drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.MOUNTED);

        assertThat(drawAction.drawUnitsFromForBattle(getApp().pbfId, getApp().playerId, 5)).hasSize(5);
        assertThat(drawAction.drawUnitsFromForBattle(getApp().pbfId, getApp().playerId, 99)).hasSize(5);
        assertThat(drawAction.drawUnitsFromForBattle(getApp().pbfId, getApp().playerId, 3)).hasSize(3);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertThat(playerhand.getBattlehand()).hasSize(3);

        drawAction.revealAndDiscardBattlehand(getApp().pbfId, getApp().playerId);

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertThat(playerhand.getBattlehand()).hasSize(0);
    }

    @Test
    public void simulateLoot() throws Exception {
        drawVillageAndMakeSureItsNoLongerInPBFCollection();

        DrawAction drawAction = new DrawAction(getApp().db);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        long nrOfVillagesP1 = 0L, nrOfVIlagesP2 = 0L;

        Playerhand playerTo = null;
        for (Playerhand players : pbf.getPlayers()) {
            if (players.getPlayerId().equals(getApp().playerId)) {
                nrOfVillagesP1 = players.getItems().stream().filter(p -> p.getSheetName() == SheetName.VILLAGES).count();
                continue;
            }
            if (playerTo == null) {
                playerTo = players;
                nrOfVIlagesP2 = players.getItems().stream().filter(p -> p.getSheetName() == SheetName.VILLAGES).count();
            }
        }

        assertThat(nrOfVillagesP1).isGreaterThan(0L);
        drawAction.drawRandomItemAndGiveToPlayer(getApp().pbfId, EnumSet.of(SheetName.VILLAGES), playerTo.getPlayerId(), getApp().playerId);

        pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        for (Playerhand players : pbf.getPlayers()) {
            if (players.getPlayerId().equals(getApp().playerId)) {
                long newVal = players.getItems().stream().filter(p -> p.getSheetName() == SheetName.VILLAGES).count();
                assertThat(nrOfVillagesP1).isGreaterThan(newVal);
            }
            if (playerTo.getPlayerId().equals(players.getPlayerId())) {
                long newVal = players.getItems().stream().filter(p -> p.getSheetName() == SheetName.VILLAGES).count();
                assertThat(nrOfVIlagesP2).isLessThan(newVal);
            }
        }
    }

}
