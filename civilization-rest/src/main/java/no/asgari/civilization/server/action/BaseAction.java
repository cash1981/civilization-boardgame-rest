package no.asgari.civilization.server.action;

import com.mongodb.DB;
import no.asgari.civilization.server.model.Draw;
import no.asgari.civilization.server.model.Spreadsheet;
import no.asgari.civilization.server.model.Tech;

public abstract class BaseAction {
    protected final GameLogAction logAction;

    protected BaseAction(DB db) {
        this.logAction = new GameLogAction(db);
    }

    /** Creates public and private logs of draws **/
    protected void createLog(Draw<? extends Spreadsheet> draw) {
        logAction.createPublicAndPrivateLog(draw);
    }

    public void createLog(Tech chosenTech, String pbfId) {
        logAction.createPublicLog(chosenTech, pbfId);
        logAction.createPrivateLog(chosenTech, pbfId);
    }
}
