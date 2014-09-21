package no.asgari.civilization.server.model;

import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;
import org.mongojack.WriteResult;

import static org.fest.assertions.api.Assertions.assertThat;

public class UndoActionTest extends AbstractMongoDBTest {

    @Test
    public void createADrawAndUndoIt() throws Exception {
        //First create one UndoAction

        //Pick one item
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getCivs()).isNotEmpty();

        Civ civ = pbf.getCivs().get(0);
        System.out.println(civ);
        Draw drawCiv = createDraw(pbfId, playerId, civ);

        UndoAction undo = new UndoAction(drawCiv.getId());
        assertThat(undo.getDrawId()).isNotEmpty();

        undo.getVotes().put(playerId, Boolean.TRUE);
        assertThat(undo.getVotes().size()).isEqualTo(1);

        WriteResult<UndoAction, String> insert = undoActionCollection.insert(undo);
        System.out.println("Created undo action " + insert.getSavedId());
    }

    private Draw createDraw(String pbfId, String playerId, Item item) {
        Draw draw = new Draw(pbfId, playerId);
        draw.setItem(item);
        WriteResult<Draw, String> insert = drawCollection.insert(draw);
        draw.setId(insert.getSavedId());
        System.out.println("Created draw " + insert.getSavedId());
        return draw;
    }
}
