package no.asgari.civilization.server.eventbus;

import static org.fest.assertions.api.Assertions.assertThat;

import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.application.CivCache;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

public class EventBusTest extends AbstractMongoDBTest {

    @Test
    public void checkThatSubscriberIsCalled() {
        PublicLog pl = new PublicLog();
        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection, undoCollection);

        long beforeInsert = publicLogCollection.count();
        //Make a draw
        Draw<Artillery> draw = drawAction.drawArtillery(pbfId, playerId);
        pl.setDraw(draw);
        pl.setPbfId(pbfId);
        pl.setUsername("cash1981");
        pl.createAndSetLog();
        CivCache.getInstance().postToEventBus(pl);

        long afterInsert = publicLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }
}
