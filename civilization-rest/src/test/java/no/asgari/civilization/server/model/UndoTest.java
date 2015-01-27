package no.asgari.civilization.server.model;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.GameAction;
import no.asgari.civilization.server.action.GameLogAction;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;

@SuppressWarnings("unchecked")
public class UndoTest extends AbstractMongoDBTest {

    private UndoAction undoAction = new UndoAction(db);

    @Before
    public void before() throws Exception {
        if (gameLogCollection.findOne() == null) {
            createADrawAndInitiateAVoteForUndo();
        }
    }

    private String createADrawAndInitiateAVoteForUndo() throws Exception {
        //First create one UndoAction

        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).isNotEmpty();

        DrawAction drawAction = new DrawAction(db);
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CIV);
        assertTrue(gameLogOptional.isPresent());
        undoAction.initiateUndo(gameLogOptional.get(), playerId);

        assertThat(gameLogCollection.findOneById(gameLogOptional.get().getId()).getDraw().getUndo().getVotes().size()).isEqualTo(1);
        return gameLogOptional.get().getId();
    }

    @Test
    public void performAVoteAndCheckIt() throws Exception {
        String gamelogId = gameLogCollection.findOne().getId();
        if(gameLogCollection.findOne().getDraw().getUndo().isDone()) {
            gamelogId = createADrawAndInitiateAVoteForUndo();
        }

        final GameLog gameLog = gameLogCollection.findOneById(gamelogId);
        assertThat(gameLog.getDraw().getUndo()).isNotNull();

        int votes = gameLog.getDraw().getUndo().getVotes().size();
        assertThat(gameLog.getDraw().getUndo().isDone()).isFalse();

        List<Playerhand> players = pbfCollection.findOneById(gameLog.getDraw().getPbfId()).getPlayers();

        Optional<Playerhand> anotherPlayer = players.stream()
                .filter(p -> !gameLog.getDraw().getUndo().getVotes().keySet().contains(p.getPlayerId()))
                .limit(1)
                .findFirst();

        assertThat(gameLog.getDraw().getUndo().getVotes().size()).isEqualTo(votes);
        votes = votes + 1;
        assertThat(anotherPlayer.isPresent()).isTrue();
        GameLog vote = undoAction.vote(gameLog, anotherPlayer.get().getPlayerId(), Boolean.TRUE);
        assertThat(vote.getDraw().getUndo().getVotes().size()).isEqualTo(votes);
        assertThat(gameLogCollection.findOneById(gameLog.getId()).getDraw().getUndo().getVotes().size()).isEqualTo(votes);
    }

    @Test
    public void allPlayersVoteYesThenPerformUndo() throws Exception {
        GameLog gameLog = gameLogCollection.findOne();
        if(gameLog.getDraw().getUndo().isDone()) {
            String gamelogId = createADrawAndInitiateAVoteForUndo();
            gameLog = gameLogCollection.findOneById(gamelogId);
        }

        PBF pbf = pbfCollection.findOneById(gameLog.getPbfId());
        assertFalse(pbf.getItems().contains(gameLog.getDraw().getItem()));
        pbf = pbfCollection.findOneById(gameLog.getPbfId());

        List<Item> items = pbf.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().get().getItems();
        assertTrue(items.contains(gameLog.getDraw().getItem()));

        final GameLog finalGameLog = gameLog;
        pbf.getPlayers().stream()
                .filter(p -> !finalGameLog.getDraw().getUndo().getVotes().keySet().contains(p.getUsername()))
                .forEach(p -> undoAction.vote(finalGameLog, p.getPlayerId(), Boolean.TRUE));

        Undo undo = gameLogCollection.findOneById(gameLog.getId()).getDraw().getUndo();
        assertThat(undo.getVotes()).doesNotContainValue(Boolean.FALSE);
        assertThat(undo.getResultOfVotes().get()).isTrue();

        //Check that item is back
        final Spreadsheet item = gameLog.getDraw().getItem();
        assertThat(item).isInstanceOf(Civ.class);

        //check that its in the pbf
        assertTrue(pbfCollection.findOneById(gameLog.getPbfId()).getItems().contains(item));
    }

    @Test
    public void checkThatYouCanUndoTech() throws Exception {
        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).isNotEmpty();

        PlayerAction playerAction = new PlayerAction(db);
        ItemDTO dto = new ItemDTO();
        dto.setName("Navy");
        dto.setPbfId(pbfId);
        dto.setOwnerId(playerId);
        dto.setSheetName(SheetName.LEVEL_1_TECH.name());

        GameLog gameLog = playerAction.chooseTech(pbfId, dto, playerId);

        assertThat(gameLog.getDraw().getUndo()).isNull();
        undoAction.initiateUndo(gameLog, playerId);
        gameLog = gameLogCollection.findOneById(gameLog.getId());
        assertThat(gameLog.getDraw().getUndo()).isNotNull();
    }

    @Test
    public void voteAndCountRemaingVotes() throws Exception{
        GameLog gameLog = gameLogCollection.findOne();
        //make another vote

        assertThat(gameLog.getDraw().getUndo()).isNotNull();
        gameLog = undoAction.vote(gameLog, getAnotherPlayerId(), Boolean.TRUE);
        assertThat(gameLog.getDraw().getUndo().votesRemaining()).isEqualTo(2);
    }

    @Test
    public void getAllActiveUndos() throws Exception {
        createADrawAndInitiateAVoteForUndo();
        List<GameLog> allActiveUndos = undoAction.getAllActiveUndos(pbfId);
        assertThat(allActiveUndos).isNotEmpty();
    }

    @Test
    public void getAllFinishedUndos() throws Exception {
        allPlayersVoteYesThenPerformUndo();
        List<GameLog> allFinishedUndos = undoAction.getAllFinishedUndos(pbfId);
        assertThat(allFinishedUndos).isNotEmpty();
    }

    private String getAnotherPlayerId() {
        Player anotherPlayer = playerCollection.findOne(DBQuery.notEquals("_id", playerId));
        assertThat(anotherPlayer).isNotNull();
        assertThat(anotherPlayer.getId()).isNotEqualTo(playerId);
        return anotherPlayer.getId();

    }
}
