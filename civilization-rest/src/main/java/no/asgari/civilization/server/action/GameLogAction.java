package no.asgari.civilization.server.action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.Level;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

/**
 * Action class responsible for logging private and public logs
 */
@Log4j
public class GameLogAction {
    private final JacksonDBCollection<GameLog, String> gameLogCollection;
    private final JacksonDBCollection<Player, String> playerCollection;

    public GameLogAction(DB db) {
        this.gameLogCollection = JacksonDBCollection.wrap(db.getCollection(GameLog.COL_NAME), GameLog.class, String.class);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
    }

    String save(@NotNull @Valid GameLog gameLog) {
        Preconditions.checkNotNull(gameLog);

        WriteResult<GameLog, String> insert = this.gameLogCollection.insert(gameLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
        return insert.getSavedId();
    }

    public GameLog createGameLog(Draw draw, GameLog.LogType logType) {
        GameLog pl = new GameLog();
        pl.setDraw(draw);
        pl.setPbfId(draw.getPbfId());
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.createAndSetLog(logType);
        pl.setId(save(pl));
        return pl;
    }

    public GameLog createGameLog(Item item, String pdfId, GameLog.LogType logType) {
        GameLog pl = new GameLog();
        Draw<Item> draw = new Draw<>(pdfId, item.getOwnerId());
        draw.setItem(item);
        pl.setDraw(draw);
        pl.setPbfId(pdfId);
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.createAndSetLog(logType);
        pl.setId(save(pl));
        return pl;
    }

    public GameLog findGameLogById(String id) {
        return gameLogCollection.findOneById(id);
    }

    private String getUsernameFromPlayerId(String playerId) {
        return playerCollection.findOneById(playerId).getUsername();
    }

    public List<GameLog> getAllPublicLogs(String pbfId) {
        @Cleanup DBCursor<GameLog> gameLogsCursor = gameLogCollection.find(DBQuery.is("pbfId", pbfId), new BasicDBObject());
        List<GameLog> gamelogs = new ArrayList<>(gameLogsCursor.size());
        while(gameLogsCursor.hasNext()) {
            gamelogs.add(gameLogsCursor.next());
        }
        return gamelogs;
    }

    public List<GameLog> getAllPrivateLogs(String pbfId, String username) {
        @Cleanup DBCursor<GameLog> gameLogsCursor = gameLogCollection.find(DBQuery.is("pbfId", pbfId).is("username", username), new BasicDBObject());
        List<GameLog> gamelogs = new ArrayList<>(gameLogsCursor.size());
        while(gameLogsCursor.hasNext()) {
            gamelogs.add(gameLogsCursor.next());
        }
        return gamelogs;
    }
}
