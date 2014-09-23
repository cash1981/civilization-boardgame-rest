package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.GameLog;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Log4j
public class GameLogAction {

    private final JacksonDBCollection<GameLog, String> collection;
    public GameLogAction(JacksonDBCollection<GameLog, String> gameLogCollection) {
        this.collection = gameLogCollection;
    }

    @SneakyThrows
    @Subscribe
    public void recordLog(@NotNull @Valid GameLog gameLog) {
        Preconditions.checkNotNull(collection);
        Preconditions.checkNotNull(gameLog);

        WriteResult<GameLog, String> insert = collection.insert(gameLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
    }
}
