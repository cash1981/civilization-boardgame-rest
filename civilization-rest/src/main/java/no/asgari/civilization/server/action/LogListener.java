package no.asgari.civilization.server.action;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Log4j
public class LogListener {

    private final JacksonDBCollection<PrivateLog, String> privateLogCollection;
    private final JacksonDBCollection<PublicLog, String> publicLogCollection;

    public LogListener(JacksonDBCollection<PrivateLog, String> gameLogCollection, JacksonDBCollection<PublicLog, String> publicLogCollection) {
        this.privateLogCollection = gameLogCollection;
        this.publicLogCollection = publicLogCollection;
    }

    @SneakyThrows
    @Subscribe
    public void recordPrivateLog(@NotNull @Valid PrivateLog privateLog) {
        Preconditions.checkNotNull(privateLogCollection);
        Preconditions.checkNotNull(privateLog);

        WriteResult<PrivateLog, String> insert = privateLogCollection.insert(privateLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
    }

    @SneakyThrows
    @Subscribe
    public void recordPublicLog(@NotNull @Valid PublicLog publicLog) {
        Preconditions.checkNotNull(publicLogCollection);
        Preconditions.checkNotNull(publicLog);

        WriteResult<PublicLog, String> insert = publicLogCollection.insert(publicLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
    }

    public static PrivateLogBuilder privateBuilder() {
        return new PrivateLogBuilder();
    }

    public static PublicLogBuilder publicBuilder() {
        return new PublicLogBuilder();
    }


    private static class PrivateLogBuilder {
        private String log;
        private String username;
        private String pbfId;
        private Draw draw;

        public PrivateLogBuilder log(String log) {
            this.log = log;
            return this;
        }

        public PrivateLogBuilder username(String username) {
            this.username = username;
            return this;
        }

        public PrivateLogBuilder pbfId(String pbfId) {
            this.pbfId = pbfId;
            return this;
        }

        public PrivateLogBuilder theDraw(Draw draw) {
            this.draw = draw;
            return this;
        }

        public String build() {
            PrivateLog privateLog = new PrivateLog();
            privateLog.setLog(log);
            privateLog.setUsername(username);
            privateLog.setPbfId(pbfId);
            privateLog.setDraw(draw);
            //TODO return recordLog
            return null;

        }

    }

    private static class PublicLogBuilder {
        //TODO create builder for logs
    }
}
