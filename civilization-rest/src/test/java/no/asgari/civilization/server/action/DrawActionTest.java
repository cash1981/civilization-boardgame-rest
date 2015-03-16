package no.asgari.civilization.server.action;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

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
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

public class DrawActionTest extends AbstractMongoDBTest {

    @Test
    public void drawCivAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        SheetName CIV = SheetName.find("CIV").get();
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, CIV);

        assertThat(gameLogOptional.isPresent()).isTrue();
        GameLog gameLog = gameLogOptional.get();
        assertThat(gameLog.getDraw().getPlayerId()).isEqualToIgnoringCase(playerId);
        assertThat(gameLog.getDraw().getItem()).isExactlyInstanceOf(Civ.class);
        assertThat(gameLog.getPrivateLog()).matches(".+drew.*Civ.+");
        assertFalse(gameLog.getPublicLog().matches(".+drew.*Civ.+"));

        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).doesNotContain(gameLog.getDraw().getItem());
    }

    @Test
    public void drawAircraftAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.AIRCRAFT);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Aircraft.class);
    }

    @Test
    public void drawArtilleryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.ARTILLERY)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Artillery.class);

        Artillery artillery = (Artillery) gameLogOptional.get().getDraw().getItem();
        assertThat(artillery.getImage()).doesNotContain(" ");
        assertThat(artillery.getImage()).endsWith(".png");
    }

    @Test
    public void drawCitystateAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CITY_STATES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Citystate.class);
    }

    @Test
    public void drawCulture1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_1);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_1)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureI.class);
    }

    @Test
    public void drawCultureI1AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_2);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_2)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureII.class);
    }

    @Test
    public void drawCulture3AndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CULTURE_3);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CULTURE_3)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(CultureIII.class);
    }

    @Test
    public void drawGPAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.GREAT_PERSON)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(GreatPerson.class);
    }

    @Test
    public void drawHutAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long hut = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.HUTS);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(hut - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.HUTS)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Hut.class);
    }

    @Test
    public void drawInfantryAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long infantries = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.INFANTRY);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(infantries - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.INFANTRY)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Infantry.class);
    }

    @Test
    public void drawMountedAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.MOUNTED);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.MOUNTED)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Mounted.class);
    }

    @Test
    public void drawTileAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.TILES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.TILES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Tile.class);
    }

    @Test
    public void drawVillageAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.VILLAGES);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.VILLAGES)
                .count());
        assertThat(gameLogOptional.get().getDraw().getItem()).isExactlyInstanceOf(Village.class);
    }

    @Test
    public void drawWonderAndMakeSureItsNoLongerInPBFCollection() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.WONDERS)
                .count();

        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.WONDERS);
        assertTrue(gameLogOptional.isPresent());
        assertEquals(aircrafts - 1, pbfCollection.findOneById(pbfId).getItems().parallelStream()
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

    @Test(expected = NoMoreItemsException.class)
    public void makeSureSystemCorrectlyThrowsExceptionWhenNothingToShuffle() throws Exception {
        DrawAction drawAction = new DrawAction(db);
        //Before draw
        long aircrafts = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        for (int i = 0; i < aircrafts; i++) {
            Optional<GameLog> draw = drawAction.draw(pbfId, playerId, SheetName.AIRCRAFT);
            assertThat(draw.isPresent());
        }

        long newCount = pbfCollection.findOneById(pbfId).getItems().parallelStream()
                .filter(p -> p.getSheetName() == SheetName.AIRCRAFT)
                .count();

        assertThat(newCount).isEqualTo(0L);

        //Now if draw again, then it should throw exception since all units are in play
        drawAction.draw(pbfId, playerId, SheetName.AIRCRAFT);
    }

    @Test
    public void drawAndDiscardBarbarians() throws Exception {
        DrawAction drawAction = new DrawAction(db);

        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().get();
        assertThat(playerhand.getBarbarians().isEmpty());

        List<Unit> units = drawAction.drawBarbarians(pbfId, playerId);
        assertThat(units).hasSize(3);

        pbf = pbfCollection.findOneById(pbfId);
        playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().get();
        assertThat(playerhand.getBarbarians()).hasSize(3);

        //Finally discard the barbs
        drawAction.discardBarbarians(pbfId, playerId);

        pbf = pbfCollection.findOneById(pbfId);
        playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().get();
        assertThat(playerhand.getBarbarians()).isEmpty();
    }

    @Test
    public void drawUnitForBattle() throws Exception {
        DrawAction drawAction = new DrawAction(db);

        drawAction.draw(pbfId, playerId, SheetName.INFANTRY);
        drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        drawAction.draw(pbfId, playerId, SheetName.MOUNTED);
        drawAction.draw(pbfId, playerId, SheetName.MOUNTED);

        assertThat(drawAction.drawUnitsFromForBattle(pbfId, playerId, 5)).hasSize(5);
        assertThat(drawAction.drawUnitsFromForBattle(pbfId, playerId, 99)).hasSize(5);
        assertThat(drawAction.drawUnitsFromForBattle(pbfId, playerId, 3)).hasSize(3);

        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().get();
        assertThat(playerhand.getBattlehand()).hasSize(3);

        drawAction.revealAndDiscardBattlehand(pbfId, playerId);

        pbf = pbfCollection.findOneById(pbfId);
        playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().get();
        assertThat(playerhand.getBattlehand()).hasSize(0);
    }

    @Test
    public void simulateLoot() throws Exception {
        drawVillageAndMakeSureItsNoLongerInPBFCollection();

        DrawAction drawAction = new DrawAction(db);

        PBF pbf = pbfCollection.findOneById(pbfId);
        long nrOfVillagesP1 = 0L, nrOfVIlagesP2 = 0L;

        Playerhand playerTo = null;
        for (Playerhand players : pbf.getPlayers()) {
            if (players.getPlayerId().equals(playerId)) {
                nrOfVillagesP1 = players.getItems().stream().filter(p -> p.getSheetName() == SheetName.VILLAGES).count();
                continue;
            }
            if (playerTo == null) {
                playerTo = players;
                nrOfVIlagesP2 = players.getItems().stream().filter(p -> p.getSheetName() == SheetName.VILLAGES).count();
            }
        }

        assertThat(nrOfVillagesP1).isGreaterThan(0L);
        drawAction.drawRandomItemAndGiveToPlayer(pbfId, SheetName.VILLAGES, playerTo.getPlayerId(), playerId);

        pbf = pbfCollection.findOneById(pbfId);
        for (Playerhand players : pbf.getPlayers()) {
            if (players.getPlayerId().equals(playerId)) {
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
