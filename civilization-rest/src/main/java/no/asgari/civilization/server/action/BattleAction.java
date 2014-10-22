package no.asgari.civilization.server.action;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.mongodb.DB;
import no.asgari.civilization.server.SheetName;
import no.asgari.civilization.server.model.GameLog;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.model.Unit;
import org.mongojack.JacksonDBCollection;

public class BattleAction extends BaseAction {
    private final JacksonDBCollection<PBF, String> pbfCollection;

    public BattleAction(DB db) {
        super(db);
        this.pbfCollection = JacksonDBCollection.wrap(db.getCollection(PBF.COL_NAME), PBF.class, String.class);
    }

    public List<Unit> drawUnitsFromHand(String pbfId, String playerId, int numberOfDraws) {
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbfId);

        List<Unit> unitsInHand = playerhand.getItems().stream()
                .filter(p -> SheetName.UNITS.contains(p.getSheetName()))
                .map(p -> (Unit) p)
                .collect(Collectors.toList());

        if(unitsInHand.isEmpty()) {
            return unitsInHand;
        }

        //Check if already units in battle
        long inBattle = unitsInHand.stream()
                .filter(Unit::isInBattle)
                .count();
        if(inBattle > 0L) {
            throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity("Some of your units are still in battle. You need to end the battle first")
                    .build());
        }

        if(unitsInHand.size() <= numberOfDraws) {
            createLog(unitsInHand, pbfId);
            return unitsInHand;
        }

        Collections.shuffle(unitsInHand);
        return unitsInHand.stream().limit(numberOfDraws)
                .peek(item -> createLog(item, pbfId, GameLog.LogType.ITEM))
                .collect(Collectors.toList());
    }

    /**
     * Sets playerhands units to isBattle = false
     * @param pbfId
     * @param playerId
     */
    public void endBattle(String pbfId, String playerId) {
        PBF pbf = pbfCollection.findOneById(pbfId);
        Playerhand playerhand = getPlayerhandByPlayerId(playerId, pbf);

        playerhand.getItems().stream()
                .filter(item -> SheetName.UNITS.contains(item.getSheetName()))
                .forEach(item -> {
                    Unit unit = (Unit) item;
                    unit.setInBattle(false);
                });

        pbfCollection.updateById(pbfId, pbf);
    }
}
