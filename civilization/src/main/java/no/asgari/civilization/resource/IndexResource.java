package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import no.asgari.civilization.representations.PBF;
import no.asgari.civilization.views.GameView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

@Path("/")
public class IndexResource {

    private JacksonDBCollection<PBF, String> collection;

    public IndexResource(JacksonDBCollection<PBF, String> blogs) {
        this.collection = blogs;
    }

    @GET
    @Produces("text/html;charset=UTF-8")
    @Timed
    public View index() {
        DBCursor<PBF> dbCursor = collection.find();
        List<PBF> PBFs = new ArrayList<>();
        while (dbCursor.hasNext()) {
            PBF PBF = dbCursor.next();
            PBFs.add(PBF);
        }
        return new GameView(PBFs);
    }

}
