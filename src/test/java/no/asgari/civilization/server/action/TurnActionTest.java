package no.asgari.civilization.server.action;

import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.PlayerTurn;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TurnActionTest extends AbstractCivilizationTest {

    @Test
    public void updateSOT() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setPhase("Sot");
        dto.setOrder("SOT: Create city @ L4");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateSOT(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);
        assertFalse(pbf.getPublicTurns().isEmpty());

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getSot());
    }

    @Test
    public void updateTrade() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setPhase("trade");
        dto.setOrder("Trade: 6 total");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateTrade(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getTrade());
    }

    @Test
    public void updateCM() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setPhase("cm");
        dto.setOrder("Trade: 6 total");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateCM(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getCm());
    }

    @Test
    public void updateMovement() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setPhase("movement");
        dto.setTurnNumber(1);
        dto.setOrder("Movement: A6 -> A5");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateMovement(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getMovement());
    }

    @Test
    public void updateResearch() {
        TurnDTO dto = new TurnDTO();
        dto.setLocked(true);
        dto.setTurnNumber(1);
        dto.setPhase("research");
        dto.setOrder("Research: Done");

        TurnAction turnAction = new TurnAction(getApp().db);
        turnAction.updateResearch(getApp().pbfId, getApp().playerId, dto);

        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().iterator().next().getResearch());
    }

}
