package no.asgari.civilization.server.action;

import com.google.common.collect.Lists;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
        log.info("Running cleanup. Finding all aborted games, chat and gamelogs from old deleted games");

        List<PBF> abortedGames = pbfCollection.find(DBQuery.is("active", false).is("winner", null)).toArray();
        log.info("Found " + abortedGames.size() + " aborted games. Deleting those.");
        //abortedGames.forEach(pbf -> pbfCollection.removeById(pbf.getId()));

        List<GameLog> allLogs = gameLogCollection.find().toArray();

        List<String> allGames = pbfCollection.find().toArray().stream().map(PBF::getId).collect(toList());

        List<GameLog> allOldLogs = allLogs.stream()
                .filter(gl -> !allGames.contains(gl.getPbfId()))
                .collect(toList());

        Set<String> oldPbfIds = allLogs.stream()
                .filter(gl -> !allGames.contains(gl.getPbfId()))
                .map(GameLog::getPbfId)
                .collect(toSet());

        log.info("Found " + allOldLogs.size() + " old logs that will be deleted");

        log.info("Before deleting, size of all game logs is " + allLogs.size());
        allOldLogs.forEach(gamelog -> gameLogCollection.removeById(gamelog.getId()));
        log.info("After deleting, size of gamelog is " + gameLogCollection.find().length());

        List<String> chatIdsToBeDeleted = chatCollection.find().toArray()
                .stream()
                .filter(chat -> oldPbfIds.contains(chat.getPbfId()))
                .map(Chat::getId)
                .collect(toList());

        log.info("Found " + chatIdsToBeDeleted.size() + " old chat ids that will be deleted");

        chatIdsToBeDeleted.forEach(id -> chatCollection.removeById(id));
        log.info("After deleting, size of chat is " + chatCollection.find().length());
    }
}
