package no.asgari.civilization.server.model;

import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class UndoTest extends AbstractMongoDBTest {

    @Before
    public void before() throws Exception {
        if(drawCollection.findOne() == null) {
            createADrawAndPerformVoteForUndo();
        }
    }

    private void createADrawAndPerformVoteForUndo() throws Exception {
        //First create one UndoAction

        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getCivs()).isNotEmpty();

        DrawAction drawAction = new DrawAction(db);
        Draw drawCiv = drawAction.drawCiv(pbfId, playerId);
        UndoAction undoAction = new UndoAction(db);
        drawCiv = undoAction.vote(drawCiv, playerId, true);

        assertThat(drawCiv.getUndo().getVotes().size()).isEqualTo(1);
    }

    @Test
    public void performAVoteAndCheckIt() throws Exception {
        Draw draw = drawCollection.findOne();
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
        Draw vote = undoAction.vote(draw, playerId.get(), Boolean.TRUE);
        assertThat(vote.getUndo().getVotes().size()).isEqualTo(votes);
        assertThat(drawCollection.findOneById(draw.getId()).getUndo().getVotes().size()).isEqualTo(votes);
    }

    @Test
    public void allPlayersVoteYesThenPerformUndo() throws Exception {
        Draw draw = drawCollection.findOne();
        PBF pbf = pbfCollection.findOneById(draw.getPbfId());
        //Get the same undo, since I need it to be final
        UndoAction undoAction = new UndoAction(db);

        pbf.getPlayers().stream()
                .filter(p -> !draw.getUndo().getVotes().keySet().contains(p.getUsername()))
                .forEach(p -> undoAction.vote(draw, p.getPlayerId(), Boolean.TRUE));

        assertThat(drawCollection.findOneById(draw.getId()).getUndo().getVotes()).doesNotContainValue(Boolean.FALSE);
        Optional<Boolean> resultOfVotes = undoAction.getResultOfVotes(draw);
        assertTrue(resultOfVotes.isPresent());
        assertThat(resultOfVotes.get()).isTrue();

        //Put item back
        final Type item = draw.getItem();
        System.out.println("Item to put back is type: " + item.getType());
        assertThat(item).isInstanceOf(Civ.class);

        //First check that its not in the pbf
        assertFalse(pbf.getCivs().contains(item));

        undoAction.putDrawnItemBackInPBF(draw);

        assertTrue(pbfCollection.findOneById(draw.getPbfId()).getCivs().contains(item));
    }

    @Test
    public void voteAndCountRemaingVotes() throws Exception{
        Draw draw = drawCollection.findOne();
        //make another vote

        UndoAction undoAction = new UndoAction(db);
        assertThat(draw.getUndo()).isNotNull();
        draw = undoAction.vote(draw, getAnotherPlayerId(), Boolean.TRUE);
        assertThat(draw.getUndo().votesRemaining()).isEqualTo(2);
    }

    private String getAnotherPlayerId() {
        Player anotherPlayer = playerCollection.findOne(DBQuery.notEquals("_id", playerId));
        assertThat(anotherPlayer).isNotNull();
        assertThat(anotherPlayer.getId()).isNotEqualTo(playerId);
        return anotherPlayer.getId();

    }
}
