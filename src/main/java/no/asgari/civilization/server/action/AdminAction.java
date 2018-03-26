package no.asgari.civilization.server.action;

import com.google.common.base.Strings;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Chat;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import org.mongojack.DBQuery;
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
        log.info("Running cleanup. Finding all aborted games, chat and gamelogs from old deleted games");

        List<String> abortedGames = pbfCollection.find(DBQuery.is("active", false).is("winner", null)).toArray().stream().map(PBF::getId).collect(toList());
        log.info("Found " + abortedGames.size() + " aborted games. Deleting those.");
        abortedGames.forEach(pbfId -> pbfCollection.removeById(pbfId));

        List<GameLog> allLogs = gameLogCollection.find().toArray();
        List<Chat> allChats = chatCollection.find().toArray();

        log.info("Before deleting, size of all game logs is " + allLogs.size());
        allLogs.parallelStream()
                .filter(gl -> abortedGames.contains(gl.getPbfId()))
                .forEach(gamelog -> gameLogCollection.removeById(gamelog.getId()));
        log.info("After deleting, size of gamelog is " + gameLogCollection.find().length());


        log.info("Before deleting chat size is " + allChats.size());

        allChats.parallelStream()
                .filter(chat -> abortedGames.contains(chat.getPbfId()) || Strings.isNullOrEmpty(chat.getPbfId()))
                .forEach(c -> chatCollection.removeById(c.getId()));

        log.info("After deleting, size of chat is " + chatCollection.find().length());

    }
}
