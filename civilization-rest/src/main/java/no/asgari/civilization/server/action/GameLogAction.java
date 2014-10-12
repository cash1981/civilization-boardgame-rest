package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Action class responsible for logging private and public logs
 */
@Log4j
public class GameLogAction {
    private final JacksonDBCollection<PrivateLog, String> privateLogCollection;
    private final JacksonDBCollection<PublicLog, String> publicLogCollection;

    public GameLogAction(DB db) {
        this.privateLogCollection = JacksonDBCollection.wrap(db.getCollection(PrivateLog.COL_NAME), PrivateLog.class, String.class);
        this.publicLogCollection = JacksonDBCollection.wrap(db.getCollection(PublicLog.COL_NAME), PublicLog.class, String.class);
    }

    public void recordPrivateLog(@NotNull @Valid PrivateLog privateLog) {
        Preconditions.checkNotNull(privateLogCollection);
        Preconditions.checkNotNull(privateLog);

        WriteResult<PrivateLog, String> insert = privateLogCollection.insert(privateLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
    }

    public void recordPublicLog(@NotNull @Valid PublicLog publicLog) {
        Preconditions.checkNotNull(publicLogCollection);
        Preconditions.checkNotNull(publicLog);

        WriteResult<PublicLog, String> insert = publicLogCollection.insert(publicLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
    }
}
