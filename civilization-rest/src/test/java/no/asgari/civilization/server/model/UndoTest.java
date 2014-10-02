package no.asgari.civilization.server.model;

import no.asgari.civilization.server.action.UndoAction;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.WriteResult;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class UndoTest extends AbstractMongoDBTest {
    private static Undo undo = null;

    @Before
    public void before() throws Exception {
        undo = undoCollection.findOne();
        if(undo == null) {
            createADrawAndUndoIt();
            undo = undoCollection.findOne();
        }
    }

    private void createADrawAndUndoIt() throws Exception {
        //First create one UndoAction

        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getCivs()).isNotEmpty();

        Civ civ = pbf.getCivs().get(0);
        System.out.println(civ);
        Draw drawCiv = createDraw(pbfId, playerId, civ);

        Undo undo = new Undo(drawCiv.getId());
        assertThat(undo.getDrawId()).isNotEmpty();

        undo.getVotes().put(playerId, Boolean.TRUE);
        assertThat(undo.getVotes().size()).isEqualTo(1);

        undo.setNumberOfVotesRequired(pbf.getNumOfPlayers());
        assertThat(undo.getNumberOfVotesRequired()).isEqualTo(pbf.getNumOfPlayers());

        WriteResult<Undo, String> insert = undoCollection.insert(undo);
        System.out.println("Created undo action " + insert.getSavedId());
    }

    @Test
    public void makeVote() throws Exception {
        undo.getVotes().put(getAnotherPlayerId(), true);
        undoCollection.updateById(undo.getId(), undo);

        assertThat(undoCollection.findOneById(undo.getId()).getVotes().size()).isEqualTo(2);
    }

    @Test
    @Ignore
    public void putUndoItemBackInPBF() throws Exception {
        Undo undo = undoCollection.findOne();
        if(undo == null) {
            createADrawAndUndoIt();
            undo = undoCollection.findOne();
        }
        //TODO
    }

    @Test
    @Ignore
    public void countRemaingVotes() throws Exception{

        //make another vote
        undo.getVotes().put("Karandras1", Boolean.FALSE);
        UndoAction action = new UndoAction(pbfCollection, drawCollection);
        assertThat(action.votesRemaining(undo)).isEqualTo(2);
    }

    private Draw createDraw(String pbfId, String playerId, Item item) {
        Draw draw = new Draw(pbfId, playerId);
        draw.setItem(item);
        WriteResult<Draw, String> insert = drawCollection.insert(draw);
        draw.setId(insert.getSavedId());
        System.out.println("Created draw " + insert.getSavedId());
        return draw;
    }

    private String getAnotherPlayerId() {
        Player anotherPlayer = playerCollection.findOne(DBQuery.notEquals("_id", playerId));
        assertThat(anotherPlayer).isNotNull();
        assertThat(anotherPlayer.getId()).isNotEqualTo(playerId);
        return anotherPlayer.getId();

    }
}
