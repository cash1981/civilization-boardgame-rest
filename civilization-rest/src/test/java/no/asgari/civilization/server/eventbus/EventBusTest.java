package no.asgari.civilization.server.eventbus;

import no.asgari.civilization.server.action.DrawAction;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class EventBusTest extends AbstractMongoDBTest {

    @Test
    public void checkThatSubscriberIsCalled() {
        PublicLog pl = new PublicLog();
        DrawAction drawAction = new DrawAction(pbfCollection, drawCollection);

        long beforeInsert = publicLogCollection.count();
        //Make a draw
        Draw<Artillery> draw = drawAction.drawArtillery(pbfId, playerId);
        pl.setDraw(draw);
        pl.setPbfId(pbfId);
        pl.setUsername("cash1981");
        pl.createAndSetLog();
        CivSingleton.getInstance().postToEventBus(pl);

        long afterInsert = publicLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }
}
