package no.asgari.civilization.server.action;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class GameLogActionTest extends AbstractMongoDBTest {

    @Test
    public void checkThatPublicLogIsSaved() {
        DrawAction drawAction = new DrawAction(db);

        long beforeInsert = gameLogCollection.count();
        //Make a draw
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.ARTILLERY);
        assertTrue(gameLogOptional.isPresent());

        assertThat(gameLogOptional.get().getDraw()).isNotNull();
        assertThat(gameLogOptional.get().getDraw().getPlayerId()).isEqualToIgnoringCase(playerId);
        assertThat(gameLogOptional.get().getDraw().getItem()).isInstanceOf(Artillery.class);
        long afterInsert = gameLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }

    @Test
    public void checkThatPrivateLogIsSaved() {
        DrawAction drawAction = new DrawAction(db);

        long beforeInsert = gameLogCollection.count();
        //Make a draw
        Optional<GameLog> gameLogOptional = drawAction.draw(pbfId, playerId, SheetName.GREAT_PERSON);
        assertThat(gameLogOptional.get().getDraw()).isNotNull();
        assertThat(gameLogOptional.get().getDraw().getPlayerId()).isEqualToIgnoringCase(playerId);
        assertThat(gameLogOptional.get().getDraw().getItem()).isInstanceOf(GreatPerson.class);
        long afterInsert = gameLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }
}
