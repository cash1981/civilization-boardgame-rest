package no.asgari.civilization.server.model;

import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.WriteResult;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class UndoTest extends AbstractMongoDBTest {
    private static Undo undo = null;

    @Before
    public void before() throws Exception {
        undo = undoCollection.findOne();
        if(undo == null) {
            createADrawAndPerformVoteForUndo();
            undo = undoCollection.findOne();
        }
    }

    private void createADrawAndPerformVoteForUndo() throws Exception {
        //First create one UndoAction

        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getCivs()).isNotEmpty();

        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection, undoCollection);
        Draw drawCiv = drawAction.drawCiv(pbfId, playerId);

        Undo undo = new Undo(drawCiv.getId());
        assertThat(undo.getDrawId()).isNotEmpty();

        undo.vote(playerId, Boolean.TRUE);
        assertThat(undo.getVotes().size()).isEqualTo(1);

        undo.setNumberOfVotesRequired(pbf.getNumOfPlayers());
        assertThat(undo.getNumberOfVotesRequired()).isEqualTo(pbf.getNumOfPlayers());

        WriteResult<Undo, String> insert = undoCollection.insert(undo);
        System.out.println("Created undo action " + insert.getSavedId());
    }

    @Test
    public void performAVoteAndCheckIt() throws Exception {
        assertThat(undoCollection.findOneById(undo.getId()).getVotes().size()).isEqualTo(1);
        undo.vote(getAnotherPlayerId(), Boolean.TRUE);
        undoCollection.updateById(undo.getId(), undo);

        assertThat(undoCollection.findOneById(undo.getId()).getVotes().size()).isEqualTo(2);
    }

    @Test
    public void allPlayersVoteYesThenPerformUndo() throws Exception {
        Draw drawToUndo = drawCollection.findOneById(undo.getDrawId());
        PBF pbf = pbfCollection.findOneById(drawToUndo.getPbfId());
        //Get the same undo, since I need it to be final
        final Undo sameUndo = undoCollection.findOneById(undo.getId());

        pbf.getPlayers().stream()
                .filter(p -> !sameUndo.getVotes().containsKey(p.getUsername()))
                .forEach(p -> sameUndo.vote(p.getUsername(), Boolean.TRUE));

        assertThat(sameUndo.getVotes()).doesNotContainValue(Boolean.FALSE);
        UndoAction undoAction = new UndoAction(pbfCollection, drawCollection);
        final Optional<Boolean> resultOfVotes = undoAction.getResultOfVotes(sameUndo);
        assertTrue(resultOfVotes.isPresent());
        assertThat(resultOfVotes.get()).isTrue();

        //First find out how many votes are done, and then each player will vote yes and undo made
        final Type item = drawToUndo.getItem();
        System.out.println("Item to put back is type: " + item.getType());
        assertThat(item).isInstanceOf(Civ.class);

        //First check that its not in the pbf
        assertFalse(pbf.getCivs().contains(item));

        undoAction.putDrawnItemBackInPBF(drawToUndo);

        assertTrue(pbfCollection.findOneById(drawToUndo.getPbfId()).getCivs().contains(item));
    }

    @Test
    @Ignore
    public void countRemaingVotes() throws Exception{

        //make another vote
        undo.getVotes().put("Karandras1", Boolean.TRUE);
        UndoAction action = new UndoAction(pbfCollection, drawCollection);
        assertThat(action.votesRemaining(undo)).isEqualTo(2);
    }

    private String getAnotherPlayerId() {
        Player anotherPlayer = playerCollection.findOne(DBQuery.notEquals("_id", playerId));
        assertThat(anotherPlayer).isNotNull();
        assertThat(anotherPlayer.getId()).isNotEqualTo(playerId);
        return anotherPlayer.getId();

    }
}
