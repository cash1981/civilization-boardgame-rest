package no.asgari.civilization.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import no.asgari.civilization.representations.Game;
import no.asgari.civilization.views.GameView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

@Path("/")
public class IndexResource {

    private JacksonDBCollection<Game, String> collection;

    public IndexResource(JacksonDBCollection<Game, String> blogs) {
        this.collection = blogs;
    }

    @GET
    @Produces("text/html;charset=UTF-8")
    @Timed
    public View index() {
        DBCursor<Game> dbCursor = collection.find();
        List<Game> games = new ArrayList<>();
        while (dbCursor.hasNext()) {
            Game game = dbCursor.next();
            games.add(game);
        }
        return new GameView(games);
    }

}
