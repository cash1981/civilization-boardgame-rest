package no.asgari.civilization.server.resource;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.application.CivCache;
import no.asgari.civilization.server.application.SimpleAuthenticator;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.Player;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    //TODO Add @Valid on PlayerDTO, right now it doesnt work
    public Response register(@Valid PlayerDTO playerDTO) {
        log.debug("Entering create player");

        PlayerAction playerAction = new PlayerAction(playerCollection, pbfCollection);
        try {
            String playerId = playerAction.createPlayer(playerDTO);
            return Response.status(Response.Status.CREATED)
                    .location(URI.create(uriInfo.getPath() + "/" + playerId)
                    ).build();
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        }
    }

    //TODO Perhaps delete should only be made to disable, and not actually delete
    @DELETE
    @Path("{id}")
    public Response deleteAccount(@PathParam("id") @NotEmpty String playerId) {
        //Throws IllegalArgumentException if id not found, so NOT_FOUND is never returned, but 500 servlet error instead
        WriteResult<Player, String> result = playerCollection.removeById(playerId);
        if(result.getError() == null)
            return Response.status(Response.Status.NO_CONTENT).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/secret")
    @Consumes(value = MediaType.APPLICATION_JSON)
    public String testSecret(@Auth(required = false) Player player) {
        return "ack";
    }

}
