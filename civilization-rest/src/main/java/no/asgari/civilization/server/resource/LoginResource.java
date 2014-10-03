package no.asgari.civilization.server.resource;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.application.CivCache;
import no.asgari.civilization.server.application.SimpleAuthenticator;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

@Path("login")
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
    @Produces(value = MediaType.TEXT_PLAIN)
    public Response login(@QueryParam("username") @NotEmpty String username, @QueryParam("password") @NotEmpty String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);

        SimpleAuthenticator auth = new SimpleAuthenticator(playerCollection);
        Optional<Player> playerOptional = auth.authenticate(new BasicCredentials(username, password));
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            URI games = uriInfo.getBaseUriBuilder()
                    .path("/player/")
                    .path(player.getId())
                    .path("/games")
                    .build();

            CivCache.getInstance().put(player);

            NewCookie usernameCookie = new NewCookie("username", player.getUsername());
            NewCookie tokenCookie = new NewCookie("token", player.getToken().toString());

            return Response.ok()
                    .location(games)
                    .cookie(usernameCookie, tokenCookie)
                    .build();
        }

        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("/secret")
    @Consumes(value = MediaType.APPLICATION_JSON)
    public String testSecret(@Auth(required = false) Player player) {
        return "ack";
    }

}
