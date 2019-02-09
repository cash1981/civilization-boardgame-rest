package no.asgari.civilization.server.action;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Civ;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Undo;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class UndoActionTest extends AbstractCivilizationTest {

    private UndoAction undoAction = new UndoAction(getApp().db);

    @Before
    public void before() {
        if (getApp().gameLogRepository.findOne() == null || getApp().gameLogRepository.findOne().getDraw() == null) {
            createADrawAndInitiateAVoteForUndo();
        }
    }

    private String createADrawAndInitiateAVoteForUndo() {
        //First create one UndoAction

        //Pick one item
        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).isNotEmpty();

        DrawAction drawAction = new DrawAction(getApp().db);
        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.CIV);
        assertTrue(gameLogOptional.isPresent());
        undoAction.initiateUndo(gameLogOptional.get(), getApp().playerId);

        assertThat(getApp().gameLogRepository.findById(gameLogOptional.get().getId()).getDraw().getUndo().getVotes().size()).isEqualTo(1);
        return gameLogOptional.get().getId();
    }

    @Test
    public void performAVoteAndCheckIt() throws Exception {
        String gamelogId = getApp().gameLogRepository.findOne().getId();
        if (getApp().gameLogRepository.findOne().getDraw().getUndo() == null || getApp().gameLogRepository.findOne().getDraw().getUndo().isDone()) {
            gamelogId = createADrawAndInitiateAVoteForUndo();
        }

        final GameLog gameLog = getApp().gameLogRepository.findById(gamelogId);
        assertThat(gameLog.getDraw().getUndo()).isNotNull();

        int votes = gameLog.getDraw().getUndo().getVotes().size();
        assertThat(gameLog.getDraw().getUndo().isDone()).isFalse();

        List<Playerhand> players = getApp().pbfRepository.findById(gameLog.getDraw().getPbfId()).getPlayers();

        Optional<Playerhand> anotherPlayer = players.stream()
                .filter(p -> !gameLog.getDraw().getUndo().getVotes().keySet().contains(p.getPlayerId()))
                .limit(1)
                .findFirst();

        assertThat(gameLog.getDraw().getUndo().getVotes().size()).isEqualTo(votes);
        votes = votes + 1;
        assertThat(anotherPlayer.isPresent()).isTrue();
        GameLog vote = undoAction.vote(gameLog, anotherPlayer.get().getPlayerId(), Boolean.TRUE);
        assertThat(vote.getDraw().getUndo().getVotes().size()).isEqualTo(votes);
        assertThat(getApp().gameLogRepository.findById(gameLog.getId()).getDraw().getUndo().getVotes().size()).isEqualTo(votes);
    }

    @Test
    public void allPlayersVoteYesThenPerformUndo() throws Exception {
        GameLog gameLog = getApp().gameLogRepository.findOne();
        if (gameLog.getDraw().getUndo() == null || gameLog.getDraw().getUndo().isDone()) {
            String gamelogId = createADrawAndInitiateAVoteForUndo();
            gameLog = getApp().gameLogRepository.findById(gamelogId);
        }

        PBF pbf = getApp().pbfRepository.findById(gameLog.getPbfId());
        assertFalse(pbf.getItems().contains(gameLog.getDraw().getItem()));
        pbf = getApp().pbfRepository.findById(gameLog.getPbfId());

        List<Item> items = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(getApp().playerId)).findFirst().get().getItems();
        assertTrue(items.contains(gameLog.getDraw().getItem()));

        final GameLog finalGameLog = gameLog;
        pbf.getPlayers().stream()
                .filter(p -> !finalGameLog.getDraw().getUndo().getVotes().keySet().contains(p.getUsername()))
                .forEach(p -> undoAction.vote(finalGameLog, p.getPlayerId(), Boolean.TRUE));

        Undo undo = getApp().gameLogRepository.findById(gameLog.getId()).getDraw().getUndo();
        assertThat(undo.getVotes()).doesNotContainValue(Boolean.FALSE);
        assertThat(undo.getResultOfVotes().get()).isTrue();

        //Check that item is back
        final Spreadsheet item = gameLog.getDraw().getItem();
        assertThat(item).isInstanceOf(Civ.class);

        //check that its in the pbf
        assertTrue(getApp().pbfRepository.findById(gameLog.getPbfId()).getItems().contains(item));
    }

    @Test
    public void checkThatYouCanUndoTech() throws Exception {
        //Pick one item
        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).isNotEmpty();

        PlayerAction playerAction = new PlayerAction(getApp().db);
        GameLog gameLog = playerAction.chooseTech(getApp().pbfId, "Navy", getApp().playerId);

        assertThat(gameLog.getDraw().getUndo()).isNull();
        undoAction.initiateUndo(gameLog, getApp().playerId);
        gameLog = getApp().gameLogRepository.findById(gameLog.getId());
        assertThat(gameLog.getDraw().getUndo()).isNotNull();
    }

    @Test
    public void voteAndCountRemaingVotes() throws Exception {
        GameLog gameLog = getApp().gameLogRepository.findOne();
        //make another vote

        assertThat(gameLog.getDraw().getUndo()).isNotNull();
        gameLog = undoAction.vote(gameLog, getAnotherPlayerId(), Boolean.TRUE);
        assertThat(gameLog.getDraw().getUndo().votesRemaining()).isEqualTo(2);
    }

    @Test
    public void getAllActiveUndos() throws Exception {
        createADrawAndInitiateAVoteForUndo();
        List<GameLog> allActiveUndos = undoAction.getAllActiveUndos(getApp().pbfId);
        assertThat(allActiveUndos).isNotEmpty();
    }

    @Test
    public void getAllFinishedUndos() throws Exception {
        allPlayersVoteYesThenPerformUndo();
        List<GameLog> allFinishedUndos = undoAction.getAllFinishedUndos(getApp().pbfId);
        assertThat(allFinishedUndos).isNotEmpty();
    }

    private String getAnotherPlayerId() {
        //Player anotherPlayer = getApp().playerRepository.findOne(DBQuery.notEquals("_id", getApp().playerId));
        Player anotherPlayer = getApp().playerRepository.findOne(DBQuery.is("username", "Itchi"));
        assertThat(anotherPlayer).isNotNull();
        assertThat(anotherPlayer.getId()).isNotEqualTo(getApp().playerId);
        return anotherPlayer.getId();

    }

}
