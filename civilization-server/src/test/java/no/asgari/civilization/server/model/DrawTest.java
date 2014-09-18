package no.asgari.civilization.server.model;

import com.mongodb.BasicDBObject;
import no.asgari.civilization.server.mongodb.MongoDBBaseTest;
import org.junit.Test;
import org.mongojack.DBCursor;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class DrawTest extends MongoDBBaseTest {

    @Test
    public void drawCivAndUpdateCollectionByRemovingTheDraw() throws Exception {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject field = new BasicDBObject();
        field.put("civs", 1); //1 means everything that matches civs, 0 means not matches
        DBCursor<PBF> cursor = super.pbfCollection.find(query, field);
        assertThat(cursor).isNotEmpty();

        List<Civ> result = new ArrayList<>();
        while (cursor.hasNext()) {
            PBF pbf = cursor.next();
            result.addAll(pbf.getCivs());
        }
        System.out.println(result);
    }

}
