package no.asgari.civilization.server.model;

import com.mongodb.BasicDBObject;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.mongojack.DBCursor;
import org.mongojack.WriteResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class DrawTest extends AbstractMongoDBTest {

    //@Test
    public void testDrawCiv() throws Exception {
        BasicDBObject where = new BasicDBObject("_id", pbfId);
        BasicDBObject select = new BasicDBObject("civ", 1);

        DBCursor<PBF> dbCursor = pbfCollection.find(where, select);
        PBF pbf = dbCursor.next();
        assertThat(pbf).isNotNull();
        assertThat(pbf.getCivs()).isNotEmpty();

    }

    //@Test
    public void drawCivAndUpdateCollectionByRemovingTheDraw() throws Exception {
        BasicDBObject field = new BasicDBObject();
        field.put("civs", 1); //1 means everything that matches civs, 0 means not matches
        field.put("_id", pbfId);
        //PBF pbf = super.pbfCollection.findOneById(pbfId, field);

        PBF pbf = pbfCollection.findAndRemove(field);
        assertThat(pbf).isNotNull();
        assertThat(pbf.getCivs()).isNotEmpty();

        //Always remove the first
        Civ civ = pbf.getCivs().remove(0);
        //TODO Whatever you draw, should be stored somewhere so that you can put it back if they revert it. Perhaps a new collection draws
        Draw<Civ> draw = new Draw<>(pbfId, playerId);
        draw.setItem(civ);
        WriteResult<Draw, String> drawWriteResult = drawCollection.insert(draw);
        assertNotNull(drawWriteResult);

    }

}
