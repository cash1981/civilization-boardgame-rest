package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.dropwizard.views.View;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.views.GameView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

@Path("/")
public class IndexResource {

    private DBCollection collection;

    public IndexResource(DBCollection collection) {
        this.collection = collection;
    }

    @GET
    @Produces("text/html;charset=UTF-8")
    @Timed
    public View index() {
        DBCursor dbCursor = collection.find();
        List<DBObject> dbObjects = new ArrayList<>();
        while (dbCursor.hasNext()) {
            DBObject dbObject = dbCursor.next();
            dbObjects.add(dbObject);
        }
        return new GameView(dbObjects);
    }

}
