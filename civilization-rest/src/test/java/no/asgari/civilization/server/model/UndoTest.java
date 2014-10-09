package no.asgari.civilization.server.model;

import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Before;
import org.junit.Test;
import org.mongojack.DBQuery;

import java.util.Optional;

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

        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection);
        Draw drawCiv = drawAction.drawCiv(pbfId, playerId);
        UndoAction undoAction = new UndoAction(pbfCollection, drawCollection);
        drawCiv = undoAction.vote(drawCiv, playerId, true);

        assertThat(drawCiv.getUndo().getVotes().size()).isEqualTo(1);
        drawCollection.insert(drawCiv);
    }

    @Test
    public void performAVoteAndCheckIt() throws Exception {
        Draw draw = drawCollection.findOne();
        assertThat(draw.getUndo()).isNotNull();
        assertThat(draw.getUndo().getVotes().size()).isEqualTo(1);
        UndoAction undoAction = new UndoAction(pbfCollection,drawCollection);

        undoAction.vote(draw, getAnotherPlayerId(), Boolean.TRUE);
        assertThat(drawCollection.findOneById(draw.getId()).getUndo().getVotes().size()).isEqualTo(2);
    }

    @Test
    public void allPlayersVoteYesThenPerformUndo() throws Exception {
        Draw draw = drawCollection.findOne();
        PBF pbf = pbfCollection.findOneById(draw.getPbfId());
        //Get the same undo, since I need it to be final
        UndoAction undoAction = new UndoAction(pbfCollection,drawCollection);

        pbf.getPlayers().stream()
                .filter(p -> !draw.getUndo().getVotes().keySet().contains(p.getUsername()))
                .forEach(p -> undoAction.vote(draw, p.getUsername(), Boolean.TRUE));

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
    public void countRemaingVotes() throws Exception{
        Draw draw = drawCollection.findOne();
        //make another vote

        UndoAction undoAction = new UndoAction(pbfCollection,drawCollection);
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
