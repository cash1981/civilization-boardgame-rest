package no.asgari.civilization.server.resource;

import com.google.common.base.Preconditions;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/login")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class LoginResource {

    private final JacksonDBCollection<Player, String> playerCollection;

    public LoginResource(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
    }

    @POST
    public Response login(@NotNull @FormParam("username") String username, @NotNull @FormParam("password") String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        String passDigest = createDigest(password);

        //TODO Add password directly in find
        DBCursor<Player> cursor = playerCollection.find(DBQuery.is("username", username));
        if(!cursor.hasNext() && !cursor.next().getPassword().equals(passDigest)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        //TODO Get base URI from somewhere
        URI games = UriBuilder.fromUri("http://localhost:8080/")
                .path("games")
                .build();
        //Include link to games?
        return Response.ok()
                .location(games)
                .build();
    }

    private String createDigest(String password) {
        return DigestUtils.sha1Hex(password);
    }
}
