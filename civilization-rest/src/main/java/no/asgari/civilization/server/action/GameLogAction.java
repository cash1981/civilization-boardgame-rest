package no.asgari.civilization.server.action;

import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivSingleton;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.PrivateLog;
import no.asgari.civilization.server.model.PublicLog;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;
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
    private final JacksonDBCollection<Player, String> playerCollection;

    public GameLogAction(DB db) {
        this.privateLogCollection = JacksonDBCollection.wrap(db.getCollection(PrivateLog.COL_NAME), PrivateLog.class, String.class);
        this.publicLogCollection = JacksonDBCollection.wrap(db.getCollection(PublicLog.COL_NAME), PublicLog.class, String.class);
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
    }

    public String save(@NotNull @Valid PrivateLog privateLog) {
        Preconditions.checkNotNull(privateLog);

        WriteResult<PrivateLog, String> insert = privateLogCollection.insert(privateLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
        return insert.getSavedId();
    }

    public String save(@NotNull @Valid PublicLog publicLog) {
        Preconditions.checkNotNull(publicLog);

        WriteResult<PublicLog, String> insert = publicLogCollection.insert(publicLog);
        log.debug("Saved Gamelog with _id " + insert.getSavedId());
        return insert.getSavedId();
    }

    public void createPublicAndPrivateLog(Draw draw) {
        createPublicLog(draw);
        createPrivateLog(draw);
    }

    public PublicLog createPublicLog(Draw draw) {
        PublicLog pl = new PublicLog();
        pl.setDraw(draw);
        pl.setPbfId(draw.getPbfId());
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.createAndSetLog();
        pl.setId(save(pl));
        return pl;
    }

    public PrivateLog createPrivateLog(Draw draw) {
        PrivateLog pl = new PrivateLog();
        pl.setDraw(draw);
        pl.setPbfId(draw.getPbfId());
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.setReveal(false);
        pl.createAndSetLog();
        pl.setId(save(pl));
        return pl;
    }

    public PublicLog createPublicLog(Tech tech, String pdfId) {
        PublicLog pl = new PublicLog();
        Draw<Spreadsheet> draw = new Draw<>(pdfId, tech.getOwnerId());
        draw.setItem(tech);
        pl.setDraw(draw);
        pl.setPbfId(pdfId);
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.createAndSetLog();
        pl.setId(save(pl));
        return pl;
    }

    public PrivateLog createPrivateLog(Tech tech, String pdfId) {
        PrivateLog pl = new PrivateLog();
        Draw<Spreadsheet> draw = new Draw<>(pdfId, tech.getOwnerId());
        draw.setItem(tech);
        pl.setDraw(draw);
        pl.setPbfId(pdfId);
        try {
            pl.setUsername(CivSingleton.instance().playerCache().get(draw.getPlayerId()));
        } catch (ExecutionException e) {
            log.error("Couldn't retrieve username from cache");
            pl.setUsername(getUsernameFromPlayerId(draw.getPlayerId()));
        }
        pl.setReveal(false);
        pl.createAndSetLog();
        pl.setId(save(pl));
        return pl;
    }

    private String getUsernameFromPlayerId(String playerId) {
        return playerCollection.findOneById(playerId).getUsername();
    }

}
