package no.asgari.civilization.server.action;

import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.PlayerTurn;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.PublicPlayerTurn;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().get(1).getSot());
    }

    @Test
    public void revealSOT() {
        TurnDTO dto = new TurnDTO();
        dto.setTurnNumber(1);
        updateSOT();
        TurnAction turnAction = new TurnAction(getApp().db);
        Collection<PublicPlayerTurn> publicPlayerTurns = turnAction.revealSOT(getApp().pbfId, getApp().playerId, dto);
        assertFalse(publicPlayerTurns.iterator().next().getSotHistory().isEmpty());

        List<PublicPlayerTurn> allPublicTurns = turnAction.getAllPublicTurns(getApp().pbfId);
        assertFalse(allPublicTurns.isEmpty());

        Collection<PlayerTurn> playersTurns = turnAction.getPlayersTurns(getApp().pbfId, getApp().playerId);
        assertFalse(playersTurns.isEmpty());
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

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);

        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().get(1).getTrade());
    }

    @Test
    public void revealTrade() {
        TurnDTO dto = new TurnDTO();
        dto.setTurnNumber(1);
        updateTrade();
        TurnAction turnAction = new TurnAction(getApp().db);
        Collection<PublicPlayerTurn> publicPlayerTurns = turnAction.revealTrade(getApp().pbfId, getApp().playerId, dto);

        assertFalse(publicPlayerTurns.iterator().next().getTradeHistory().isEmpty());

        List<PublicPlayerTurn> allPublicTurns = turnAction.getAllPublicTurns(getApp().pbfId);
        assertFalse(allPublicTurns.isEmpty());

        Collection<PlayerTurn> playersTurns = turnAction.getPlayersTurns(getApp().pbfId, getApp().playerId);
        assertFalse(playersTurns.isEmpty());
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

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().get(1).getCm());
    }

    @Test
    public void revealCM() {
        TurnDTO dto = new TurnDTO();
        dto.setTurnNumber(1);
        updateCM();
        TurnAction turnAction = new TurnAction(getApp().db);
        Collection<PublicPlayerTurn> publicPlayerTurns = turnAction.revealCM(getApp().pbfId, getApp().playerId, dto);

        assertFalse(publicPlayerTurns.iterator().next().getCmHistory().isEmpty());

        List<PublicPlayerTurn> allPublicTurns = turnAction.getAllPublicTurns(getApp().pbfId);
        assertFalse(allPublicTurns.isEmpty());

        Collection<PlayerTurn> playersTurns = turnAction.getPlayersTurns(getApp().pbfId, getApp().playerId);
        assertFalse(playersTurns.isEmpty());
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

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().get(1).getMovement());
    }

    @Test
    public void revealMovement() {
        TurnDTO dto = new TurnDTO();
        dto.setTurnNumber(1);
        updateMovement();
        TurnAction turnAction = new TurnAction(getApp().db);
        Collection<PublicPlayerTurn> publicPlayerTurns = turnAction.revealMovement(getApp().pbfId, getApp().playerId, dto);

        assertFalse(publicPlayerTurns.iterator().next().getMovementHistory().isEmpty());

        List<PublicPlayerTurn> allPublicTurns = turnAction.getAllPublicTurns(getApp().pbfId);
        assertFalse(allPublicTurns.isEmpty());

        Collection<PlayerTurn> playersTurns = turnAction.getPlayersTurns(getApp().pbfId, getApp().playerId);
        assertFalse(playersTurns.isEmpty());
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

        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        assertEquals(dto.getOrder(), playerhand.getPlayerTurns().get(1).getResearch());
    }

    @Test
    public void revealResearch() {
        TurnDTO dto = new TurnDTO();
        dto.setTurnNumber(1);
        updateResearch();
        TurnAction turnAction = new TurnAction(getApp().db);
        Collection<PublicPlayerTurn> publicPlayerTurns = turnAction.revealResearch(getApp().pbfId, getApp().playerId, dto);

        assertFalse(publicPlayerTurns.iterator().next().getResearchHistory().isEmpty());

        List<PublicPlayerTurn> allPublicTurns = turnAction.getAllPublicTurns(getApp().pbfId);
        assertFalse(allPublicTurns.isEmpty());

        Collection<PlayerTurn> playersTurns = turnAction.getPlayersTurns(getApp().pbfId, getApp().playerId);
        assertFalse(playersTurns.isEmpty());
    }

    @Test
    public void lockTurn() throws Exception {
        PBF pbf = getApp().pbfCollection.findOneById(getApp().pbfId);
        Playerhand playerhand = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get();
        int nr = playerhand.getPlayerTurns().size();
        TurnDTO dto = new TurnDTO();
        dto.setTurnNumber(1);
        updateResearch();
        TurnAction turnAction = new TurnAction(getApp().db);
        Collection<PlayerTurn> playerTurns = turnAction.lockOrUnlockTurn(getApp().pbfId, getApp().playerId, dto);
        assertEquals(nr+1, playerTurns.size());
    }

}
