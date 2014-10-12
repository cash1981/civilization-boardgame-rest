package no.asgari.civilization.server.eventbus;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.action.GameLogAction;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;

public class GameLogActionTest extends AbstractMongoDBTest {

    @Test
    public void checkThatPublicLogIsSaved() {
        DrawAction drawAction = new DrawAction(db);

        long beforeInsert = publicLogCollection.count();
        //Make a draw
        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        PublicLog pl = new PublicLog();
        pl.setDraw(drawOptional.get());
        pl.setPbfId(pbfId);
        pl.setUsername("cash1981");
        pl.createAndSetLog();

        long afterInsert = publicLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }

    @Test
    public void checkThatPrivateLogIsSaved() {
        DrawAction drawAction = new DrawAction(db);

        long beforeInsert = privateLogCollection.count();
        //Make a draw
        Optional<Draw<? extends Spreadsheet>> drawOptional = drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        PrivateLog pl = new PrivateLog();
        pl.setDraw(drawOptional.get());
        pl.setPbfId(pbfId);
        pl.setUsername("cash1981");
        pl.createAndSetLog();

        long afterInsert = privateLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }
}
