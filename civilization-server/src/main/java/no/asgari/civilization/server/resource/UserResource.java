package no.asgari.civilization.server.resource;

import lombok.extern.log4j.Log4j;
import no.asgari.civilization.server.dto.PlayerDTO;
import no.asgari.civilization.server.model.Player;
import org.mongojack.JacksonDBCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/user")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Log4j
public class UserResource {

    private final JacksonDBCollection<Player, String> playerCollection;

    public UserResource(JacksonDBCollection<Player, String> playerCollection) {
        this.playerCollection = playerCollection;
    }

    @POST
    public Response createPlayer(PlayerDTO playerDTO) {
        log.debug("Entering create player");
        return Response.status(Response.Status.CREATED).build();
    }
}
