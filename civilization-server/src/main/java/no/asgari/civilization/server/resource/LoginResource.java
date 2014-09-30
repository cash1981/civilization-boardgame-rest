package no.asgari.civilization.server.resource;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.SimpleAuthenticator;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/login")
@Log4j
@Produces(value = MediaType.APPLICATION_JSON)
public class LoginResource {

    private final JacksonDBCollection<Player, String> playerCollection;

    @Context
    private UriInfo uriInfo;

    public LoginResource(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
    }

    @POST
    @Consumes(value = MediaType.TEXT_PLAIN)
    public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);

        String passDigest = createDigest(password);
        log.debug("Passdigest is " + passDigest);

        SimpleAuthenticator auth = new SimpleAuthenticator(playerCollection);
        Optional<Player> playerOptional = auth.authenticate(new BasicCredentials(username, passDigest));
        if (playerOptional.isPresent()) {
            URI games = uriInfo.getAbsolutePathBuilder()
                    .path("/games")
                    .build();
            return Response.ok()
                    .location(games)
                    .entity(playerOptional.get())
                    .build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /*@GET
    @Path("/test")
    @Consumes(value = MediaType.APPLICATION_JSON)
    public Response testSecret(@Auth(required = false) Player player) {
        return Response.ok().build();
    }
    */



    private String createDigest(String password) {
        return DigestUtils.sha1Hex(password);
    }
}
