package no.asgari.civilization.server.model;

import com.mongodb.BasicDBObject;
import no.asgari.civilization.server.mongodb.MongoDBBaseTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class DrawTest extends MongoDBBaseTest {

    @Test
    @Ignore
    public void drawCivAndUpdateCollectionByRemovingTheDraw() throws Exception {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject field = new BasicDBObject();
        field.put("civs", 1); //1 means everything that matches civs, 0 means not matches
        PBF pbf = super.pbfCollection.findOneById(pbfId, field);
        assertThat(pbf).isNotNull();

        assertThat(pbf.getCivs()).isNotEmpty();

        //Always remove the first
        Civ civ = pbf.getCivs().remove(0);
        //TODO Whatever you draw, should be stored somewhere so that you can put it back if they revert it. Perhaps a new collection draws
        Draw<Civ> draw = new Draw<>(playerId, pbfId);
        draw.setItem(civ);
    }

    private Draw createDraw(Spreadsheet item) {
       return null;
    }

}
