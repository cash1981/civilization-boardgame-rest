package no.asgari.civilization.server.action;

import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TurnActionTest extends AbstractCivilizationTest {

    @Test
    public void updateAndLockSOT() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setOrder("SOT: Create city @ L4");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateAndLockSOT(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        assertFalse(pbf.getPublicTurns().isEmpty());

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getSot());
    }

    @Test
    public void updateAndLockTrade() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setOrder("Trade: 6 total");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateAndLockTrade(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getTrade());
    }

    @Test
    public void updateAndLockCM() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setOrder("Trade: 6 total");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateAndLockCM(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getCm());
    }

    @Test
    public void updateAndLockMovement() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setOrder("Movement: A6 -> A5");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateAndLockMovement(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getMovement());
    }

    @Test
    public void updateAndLockResearch() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setOrder("Research: Done");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateAndLockResearch(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getResearch());
    }

}
