package no.asgari.civilization.server.action;

import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.Artillery;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.GreatPerson;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class GameLogActionTest extends AbstractCivilizationTest {

    @Test
    public void checkThatPublicLogIsSaved() {
        DrawAction drawAction = new DrawAction(getApp().db);

        long beforeInsert = getApp().gameLogCollection.count();
        //Make a draw
        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.ARTILLERY);
        assertTrue(gameLogOptional.isPresent());

        assertThat(gameLogOptional.get().getDraw()).isNotNull();
        assertThat(gameLogOptional.get().getDraw().getPlayerId()).isEqualToIgnoringCase(getApp().playerId);
        assertThat(gameLogOptional.get().getDraw().getItem()).isInstanceOf(Artillery.class);
        long afterInsert = getApp().gameLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }

    @Test
    public void checkThatPrivateLogIsSaved() {
        DrawAction drawAction = new DrawAction(getApp().db);

        long beforeInsert = getApp().gameLogCollection.count();
        //Make a draw
        Optional<GameLog> gameLogOptional = drawAction.draw(getApp().pbfId, getApp().playerId, SheetName.GREAT_PERSON);
        assertThat(gameLogOptional.get().getDraw()).isNotNull();
        assertThat(gameLogOptional.get().getDraw().getPlayerId()).isEqualToIgnoringCase(getApp().playerId);
        assertThat(gameLogOptional.get().getDraw().getItem()).isInstanceOf(GreatPerson.class);
        long afterInsert = getApp().gameLogCollection.count();
        assertThat(beforeInsert).isLessThan(afterInsert);
    }

}
