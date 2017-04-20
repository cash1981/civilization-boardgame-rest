package no.asgari.civilization.server.action;

import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import org.mongojack.JacksonDBCollection;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Log4j
public class AdminAction extends BaseAction {

    private final JacksonDBCollection<Chat, String> chatCollection;
    private final JacksonDBCollection<GameLog, String> gameLogCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public AdminAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
        this.chatCollection = JacksonDBCollection.wrap(db.getCollection(Chat.COL_NAME), Chat.class, String.class);
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
    }

    public void cleanup() {
        log.info("Running cleanup. Finding all unused chat and gamelogs from old deleted games");

        List<GameLog> allLogs = gameLogCollection.find().toArray();
        List<String> allGames = pbfCollection.find().toArray().stream().map(PBF::getId).collect(toList());

        List<GameLog> allOldLogs = allLogs.stream()
                .filter(gl -> !allGames.contains(gl.getPbfId()))
                .collect(toList());

        List<String> oldPbfIds = allLogs.stream()
                .filter(gl -> !allGames.contains(gl.getPbfId()))
                .map(GameLog::getPbfId)
                .collect(toList());

        log.info("Found " + allOldLogs.size() + " old logs that will be deleted");


        List<String> chatIdsToBeDeleted = chatCollection.find().toArray()
                .stream()
                .filter(chat -> oldPbfIds.contains(chat.getPbfId()))
                .map(Chat::getId)
                .collect(toList());

        log.info("Found " + chatIdsToBeDeleted.size() + " old chat ids that will be deleted");
    }
}
