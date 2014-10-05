package no.asgari.civilization.server.resource;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.action.PlayerAction;
import no.asgari.civilization.server.application.CivAuthenticator;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Player;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

@Path("login")
@Log4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    private final JacksonDBCollection<Player, String> playerCollection;
    private final JacksonDBCollection<PBF, String> pbfCollection;

    @Context
    private UriInfo uriInfo;

    public LoginResource(JacksonDBCollection<Player, String> playerCollection, JacksonDBCollection<PBF, String> pbfCollection) {
        this.playerCollection = playerCollection;
        this.pbfCollection = pbfCollection;
    }

    @POST
    @Consumes(value = MediaType.TEXT_PLAIN)
    @Produces(value = MediaType.TEXT_PLAIN)
    public Response login(@QueryParam("username") @NotEmpty String username, @QueryParam("password") @NotEmpty String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);

        CivAuthenticator auth = new CivAuthenticator(playerCollection);
        Optional<Player> playerOptional = auth.authenticate(new BasicCredentials(username, password));
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            URI games = uriInfo.getBaseUriBuilder()
                    .path("/player/")
                    .path(player.getId())
                    .path("/games")
                    .build();

            return Response.ok()
                    .location(games)
                    .build();
        }

        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Valid PlayerDTO playerDTO) {
        Preconditions.checkNotNull(playerDTO);
        log.debug("Entering create player");

        PlayerAction playerAction = new PlayerAction(playerCollection, pbfCollection);
        try {
            String playerId = playerAction.createPlayer(playerDTO);
            return Response.status(Response.Status.CREATED)
                    .location(uriInfo.getAbsolutePathBuilder().path(playerId).build())
                    .build();
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        }  catch (Exception ex) {
            log.error("Unknown error when registering user: " + ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    //TODO Perhaps delete should only be made to disable, and not actually delete
    @DELETE
    @Path("{id}")
    public Response deleteAccount(@PathParam("id") @NotEmpty String playerId) {
        //Throws IllegalArgumentException if id not found, so NOT_FOUND is never returned, but 500 servlet error instead
        try {
            WriteResult<Player, String> result = playerCollection.removeById(playerId);
            if (result.getError() == null)
                return Response.status(Response.Status.NO_CONTENT).build();
        } catch(Exception ex) {
            log.error("Unknown error when deleting user: " + ex.getMessage(), ex);
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

}
