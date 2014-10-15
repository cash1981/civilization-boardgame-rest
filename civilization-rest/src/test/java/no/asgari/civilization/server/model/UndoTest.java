package no.asgari.civilization.server.model;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;

@SuppressWarnings("unchecked")
public class UndoTest extends AbstractMongoDBTest {

    @Before
    public void before() throws Exception {
        if (gameLogCollection.findOne() == null) {
            createADrawAndPerformVoteForUndo();
        }
    }

    private void createADrawAndPerformVoteForUndo() throws Exception {
        //First create one UndoAction

        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getItems()).isNotEmpty();

        DrawAction drawAction = new DrawAction(db);
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.CIV);
        assertTrue(gameLogOptional.isPresent());
        UndoAction undoAction = new UndoAction(db);
        GameLog gameLog = undoAction.vote(gameLogOptional.get(), playerId, true);

        assertThat(gameLog.getDraw().getUndo().getVotes().size()).isEqualTo(1);
    }

    @Test
    public void performAVoteAndCheckIt() throws Exception {
        GameLog gameLog = gameLogCollection.findOne();
        Draw draw = gameLog.getDraw();
        int votes = draw.getUndo().getVotes().size();
        assertThat(draw.getUndo()).isNotNull();
        UndoAction undoAction = new UndoAction(db);

        List<Playerhand> players = pbfCollection.findOneById(draw.getPbfId()).getPlayers();
        Set<String> playerIds = draw.getUndo().getVotes().keySet();
        Optional<String> playerId = players.stream()
                .map(p -> p.getPlayerId())
                .filter(p -> !playerIds.contains(p))
                .limit(1)
                .findFirst();

        votes = votes + 1;
        assertThat(playerId.isPresent()).isTrue();
        GameLog vote = undoAction.vote(gameLog, playerId.get(), Boolean.TRUE);
        assertThat(vote.getDraw().getUndo().getVotes().size()).isEqualTo(votes);
        assertThat(gameLogCollection.findOneById(gameLog.getId()).getDraw().getUndo().getVotes().size()).isEqualTo(votes);
    }

    @Test
    public void allPlayersVoteYesThenPerformUndo() throws Exception {
        GameLog gameLog = gameLogCollection.findOne();
        PBF pbf = pbfCollection.findOneById(gameLog.getPbfId());
        //Get the same undo, since I need it to be final
        UndoAction undoAction = new UndoAction(db);

        pbf.getPlayers().stream()
                .filter(p -> !gameLog.getDraw().getUndo().getVotes().keySet().contains(p.getUsername()))
                .forEach(p -> undoAction.vote(gameLog, p.getPlayerId(), Boolean.TRUE));

        assertThat(gameLogCollection.findOneById(gameLog.getId()).getDraw().getUndo().getVotes()).doesNotContainValue(Boolean.FALSE);
        Optional<Boolean> resultOfVotes = undoAction.getResultOfVotes(gameLog.getDraw());
        assertTrue(resultOfVotes.isPresent());
        assertThat(resultOfVotes.get()).isTrue();

        //Put item back
        final Spreadsheet item = gameLog.getDraw().getItem();
        System.out.println("Item to put back is type: " + item.getSheetName());
        assertThat(item).isInstanceOf(Civ.class);

        //First check that its not in the pbf
        assertFalse(pbf.getItems().contains(item));

        undoAction.putDrawnItemBackInPBF(gameLog.getDraw());

        assertTrue(pbfCollection.findOneById(gameLog.getPbfId()).getItems().contains(item));
    }

    @Test
    public void voteAndCountRemaingVotes() throws Exception{
        GameLog gameLog = gameLogCollection.findOne();
        //make another vote

        UndoAction undoAction = new UndoAction(db);
        assertThat(gameLog.getDraw().getUndo()).isNotNull();
        gameLog = undoAction.vote(gameLog, getAnotherPlayerId(), Boolean.TRUE);
        assertThat(gameLog.getDraw().getUndo().votesRemaining()).isEqualTo(2);
    }

    private String getAnotherPlayerId() {
        Player anotherPlayer = playerCollection.findOne(DBQuery.notEquals("_id", playerId));
        assertThat(anotherPlayer).isNotNull();
        assertThat(anotherPlayer.getId()).isNotEqualTo(playerId);
        return anotherPlayer.getId();

    }
}
